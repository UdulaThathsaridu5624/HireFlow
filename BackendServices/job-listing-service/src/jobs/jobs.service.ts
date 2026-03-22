import {
  Injectable,
  NotFoundException,
  ForbiddenException,
  Logger,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Job } from './entities/job.entity';
import { CreateJobDto } from './dto/create-job.dto';
import { UpdateJobDto } from './dto/update-job.dto';
import { SearchJobsDto } from './dto/search-jobs.dto';
import { CompanyClientService } from '../company-client/company-client.service';
import { MessagingService } from '../messaging/messaging.service';
import { JobStatus } from '../constants';

@Injectable()
export class JobsService {
  private readonly logger = new Logger(JobsService.name);

  constructor(
    @InjectRepository(Job)
    private readonly jobRepo: Repository<Job>,
    private readonly companyClient: CompanyClientService,
    private readonly messagingService: MessagingService, // 👈 added
  ) {}

  // ─────────────────────────────────────────────
  // EMPLOYER ACTIONS
  // ─────────────────────────────────────────────

  async create(dto: CreateJobDto, employerId: string, token: string): Promise<Job> {
    const company = await this.companyClient.getCompanyByEmployerId(employerId, token);

    const job = this.jobRepo.create({
      ...dto,
      deadline: new Date(dto.deadline),
      employerId,
      companyId: company?.id ?? null,
      status: JobStatus.OPEN,
    });

    const saved = await this.jobRepo.save(job);
    this.logger.log(`Job created: ${saved.id} by employer ${employerId}`);

    // 👇 Publish event — interview service / other services can react to this
    await this.messagingService.publishJobPosted({
      jobId: saved.id,
      employerId,
      title: saved.title,
      location: saved.location,
      isRemote: saved.isRemote,
    });

    return saved;
  }

  async update(id: string, dto: UpdateJobDto, employerId: string): Promise<Job> {
    const job = await this.findOneOrFail(id);
    this.assertOwnership(job, employerId);

    const updated = this.jobRepo.merge(job, {
      ...dto,
      ...(dto.deadline && { deadline: new Date(dto.deadline) }),
    });

    return this.jobRepo.save(updated);
  }

  async closeJob(id: string, employerId: string): Promise<Job> {
    const job = await this.findOneOrFail(id);
    this.assertOwnership(job, employerId);
    job.status = JobStatus.CLOSED;
    const saved = await this.jobRepo.save(job);

    // 👇 Publish event — notify other services that this job is no longer accepting applications
    await this.messagingService.publishJobClosed(id, employerId);

    return saved;
  }

  async findAllByEmployer(employerId: string, token: string): Promise<any[]> {
    const jobs = await this.jobRepo.find({
      where: { employerId },
      order: { createdAt: 'DESC' },
    });

    const company = await this.companyClient.getCompanyByEmployerId(employerId, token);
    return jobs.map((job) => ({ ...job, company: company ?? null }));
  }

  // ─────────────────────────────────────────────
  // CANDIDATE ACTIONS
  // ─────────────────────────────────────────────

  async findAllOpen(token: string): Promise<any[]> {
    const jobs = await this.jobRepo.find({
      where: { status: JobStatus.OPEN },
      order: { createdAt: 'DESC' },
    });

    const enriched = await Promise.all(
      jobs.map(async (job) => {
        const company = job.companyId
          ? await this.companyClient.getCompanyById(job.companyId, token)
          : null;
        return { ...job, company };
      }),
    );

    return enriched;
  }

  async search(filters: SearchJobsDto, token: string): Promise<any[]> {
    const query = this.jobRepo
      .createQueryBuilder('job')
      .where('job.status = :status', { status: JobStatus.OPEN });

    if (filters.title) {
      query.andWhere('job.title ILIKE :title', { title: `%${filters.title}%` });
    }
    if (filters.location) {
      query.andWhere('job.location ILIKE :location', { location: `%${filters.location}%` });
    }
    if (filters.skill) {
      query.andWhere('job.requiredSkills ILIKE :skill', { skill: `%${filters.skill}%` });
    }
    if (filters.salaryMin !== undefined) {
      query.andWhere('job.salaryMax >= :salaryMin', { salaryMin: filters.salaryMin });
    }
    if (filters.salaryMax !== undefined) {
      query.andWhere('job.salaryMin <= :salaryMax', { salaryMax: filters.salaryMax });
    }

    query.orderBy('job.createdAt', 'DESC');
    const jobs = await query.getMany();

    const enriched = await Promise.all(
      jobs.map(async (job) => {
        const company = job.companyId
          ? await this.companyClient.getCompanyById(job.companyId, token)
          : null;
        return { ...job, company };
      }),
    );

    return enriched;
  }

  async findOneWithCompany(id: string, token: string): Promise<any> {
    const job = await this.findOneOrFail(id);
    const company = job.companyId
      ? await this.companyClient.getCompanyById(job.companyId, token)
      : null;
    return { ...job, company };
  }

  // ─────────────────────────────────────────────
  // INTERNAL HELPERS
  // ─────────────────────────────────────────────

  async findOneOrFail(id: string): Promise<Job> {
    const job = await this.jobRepo.findOne({ where: { id } });
    if (!job) throw new NotFoundException(`Job with id ${id} not found`);
    return job;
  }

  private assertOwnership(job: Job, employerId: string): void {
    if (job.employerId !== employerId) {
      throw new ForbiddenException('You do not own this job posting');
    }
  }

  async verifyJobIsOpen(id: string): Promise<{ isOpen: boolean; job: Job | null }> {
    try {
      const job = await this.findOneOrFail(id);
      return { isOpen: job.status === JobStatus.OPEN, job };
    } catch {
      return { isOpen: false, job: null };
    }
  }
}