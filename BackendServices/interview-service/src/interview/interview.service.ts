import {
  ForbiddenException,
  Inject,
  Injectable,
  Logger,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { ClientProxy } from '@nestjs/microservices';
import { Interview } from './entities/interview.entity';
import { HiringPipeline } from './entities/hiring-pipeline.entity';
import { CreateInterviewDto } from './dto/create-interview.dto';
import { UpdateInterviewDto } from './dto/update-interview.dto';
import { InterviewEvents, InterviewStatus, ServiceTokens } from '../constants';

@Injectable()
export class InterviewService {
  private readonly logger = new Logger(InterviewService.name);

  constructor(
    @InjectRepository(Interview)
    private readonly interviewRepository: Repository<Interview>,
    @InjectRepository(HiringPipeline)
    private readonly pipelineRepository: Repository<HiringPipeline>,
    @Inject(ServiceTokens.INTERVIEW_PUBLISHER)
    private readonly publisher: ClientProxy,
  ) {}

  async scheduleInterview(dto: CreateInterviewDto, interviewerId: string): Promise<Interview> {
    const pipeline = await this.pipelineRepository.findOne({
      where: { applicationId: dto.applicationId },
    });
    if (!pipeline) {
      throw new NotFoundException(`No pipeline found for application ${dto.applicationId}`);
    }

    const interview = this.interviewRepository.create({
      jobId: pipeline.jobId,
      candidateId: pipeline.candidateId,
      interviewerId,
      scheduledAt: new Date(dto.scheduledAt),
      durationMinutes: dto.durationMinutes,
      format: dto.format,
      meetingLink: dto.meetingLink,
      notes: dto.notes,
      status: InterviewStatus.SCHEDULED,
    });
    const saved = await this.interviewRepository.save(interview);
    this.publisher.emit(InterviewEvents.INTERVIEW_SCHEDULED, saved);
    this.logger.log(`Interview scheduled: ${saved.id}`);
    return saved;
  }

  async updateInterview(id: string, dto: UpdateInterviewDto, userId: string): Promise<Interview> {
    const interview = await this.findOneOrFail(id);
    if (interview.interviewerId !== userId && interview.candidateId !== userId) {
      throw new ForbiddenException('Not authorized to update this interview');
    }
    Object.assign(interview, dto);
    const saved = await this.interviewRepository.save(interview);
    this.publisher.emit(InterviewEvents.INTERVIEW_UPDATED, saved);
    return saved;
  }

  async cancelInterview(id: string, userId: string): Promise<Interview> {
    const interview = await this.findOneOrFail(id);
    if (interview.interviewerId !== userId) {
      throw new ForbiddenException('Only the interviewer can cancel');
    }
    interview.status = InterviewStatus.CANCELLED;
    const saved = await this.interviewRepository.save(interview);
    this.publisher.emit(InterviewEvents.INTERVIEW_CANCELLED, saved);
    return saved;
  }

  async acceptInterview(id: string, candidateId: string): Promise<Interview> {
    const interview = await this.findOneOrFail(id);
    if (interview.candidateId !== candidateId) {
      throw new ForbiddenException('Not your interview to accept');
    }
    interview.status = InterviewStatus.ACCEPTED;
    return this.interviewRepository.save(interview);
  }

  async declineInterview(id: string, candidateId: string): Promise<Interview> {
    const interview = await this.findOneOrFail(id);
    if (interview.candidateId !== candidateId) {
      throw new ForbiddenException('Not your interview to decline');
    }
    interview.status = InterviewStatus.DECLINED;
    return this.interviewRepository.save(interview);
  }

  async getByEmployer(employerId: string): Promise<Interview[]> {
    return this.interviewRepository.find({ where: { interviewerId: employerId } });
  }

  async getByCandidate(candidateId: string): Promise<Interview[]> {
    return this.interviewRepository.find({ where: { candidateId } });
  }

  private async findOneOrFail(id: string): Promise<Interview> {
    const interview = await this.interviewRepository.findOne({ where: { id } });
    if (!interview) throw new NotFoundException(`Interview ${id} not found`);
    return interview;
  }
}
