import {
  Injectable,
  NotFoundException,
  BadRequestException,
  ForbiddenException,
  ConflictException,
  Logger,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Application } from './entities/application.entity';
import { ApplyToJobDto } from './dto/apply-to-job.dto';
import { JobsService } from '../jobs/jobs.service';
import { ApplicationStatus } from '../constants';

@Injectable()
export class ApplicationsService {
  private readonly logger = new Logger(ApplicationsService.name);

  constructor(
    @InjectRepository(Application)
    private readonly appRepo: Repository<Application>,
    private readonly jobsService: JobsService,
  ) {}

  // ─────────────────────────────────────────────
  // CANDIDATE ACTIONS
  // ─────────────────────────────────────────────

  async apply(jobId: string, dto: ApplyToJobDto, candidateId: string): Promise<Application> {
    // Verify job is open before allowing application
    const { isOpen, job } = await this.jobsService.verifyJobIsOpen(jobId);
    if (!job) throw new NotFoundException(`Job ${jobId} not found`);
    if (!isOpen) throw new BadRequestException('This job posting is no longer accepting applications');

    // Prevent duplicate applications
    const existing = await this.appRepo.findOne({ where: { candidateId, jobId } });
    if (existing) throw new ConflictException('You have already applied to this job');

    const application = this.appRepo.create({
      ...dto,
      candidateId,
      jobId,
      status: ApplicationStatus.PENDING,
    });

    const saved = await this.appRepo.save(application);
    this.logger.log(`Candidate ${candidateId} applied to job ${jobId}`);
    return saved;
  }

  async getMyApplications(candidateId: string): Promise<Application[]> {
    return this.appRepo.find({
      where: { candidateId },
      relations: ['job'],
      order: { appliedAt: 'DESC' },
    });
  }

  async withdraw(applicationId: string, candidateId: string): Promise<Application> {
    const application = await this.findOneOrFail(applicationId);
    this.assertCandidateOwnership(application, candidateId);

    if (application.status !== ApplicationStatus.PENDING) {
      throw new BadRequestException('Only pending applications can be withdrawn');
    }

    application.status = ApplicationStatus.WITHDRAWN;
    return this.appRepo.save(application);
  }

  // ─────────────────────────────────────────────
  // EMPLOYER ACTIONS
  // ─────────────────────────────────────────────

  async getApplicationsForJob(jobId: string, employerId: string): Promise<Application[]> {
    // Verify employer owns this job
    await this.jobsService.findOneOrFail(jobId);
    const job = await this.jobsService.findOneOrFail(jobId);
    if (job.employerId !== employerId) {
      throw new ForbiddenException('You do not own this job posting');
    }

    return this.appRepo.find({
      where: { jobId },
      order: { appliedAt: 'DESC' },
    });
  }

  async getApplicationById(applicationId: string, employerId: string): Promise<Application> {
    const application = await this.findOneOrFail(applicationId);
    const job = await this.jobsService.findOneOrFail(application.jobId);

    if (job.employerId !== employerId) {
      throw new ForbiddenException('You do not have access to this application');
    }

    return application;
  }

  // ─────────────────────────────────────────────
  // HELPERS
  // ─────────────────────────────────────────────

  async findOneOrFail(id: string): Promise<Application> {
    const app = await this.appRepo.findOne({ where: { id }, relations: ['job'] });
    if (!app) throw new NotFoundException(`Application ${id} not found`);
    return app;
  }

  private assertCandidateOwnership(application: Application, candidateId: string): void {
    if (application.candidateId !== candidateId) {
      throw new ForbiddenException('You do not own this application');
    }
  }
}
