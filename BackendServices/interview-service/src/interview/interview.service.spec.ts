import { ForbiddenException, NotFoundException } from '@nestjs/common';
import { Test, TestingModule } from '@nestjs/testing';
import { getRepositoryToken } from '@nestjs/typeorm';
import { InterviewService } from './interview.service';
import { Interview } from './entities/interview.entity';
import { HiringPipeline } from './entities/hiring-pipeline.entity';
import { InterviewFormat, InterviewStatus, ServiceTokens } from '../constants';

const mockInterview = (overrides = {}): Interview =>
  ({
    id: 'int-001',
    jobId: 'job-001',
    candidateId: 'cand-001',
    interviewerId: 'emp-001',
    scheduledAt: new Date('2026-04-01T10:00:00Z'),
    durationMinutes: 60,
    format: InterviewFormat.ONLINE,
    status: InterviewStatus.SCHEDULED,
    ...overrides,
  }) as Interview;

const mockPipeline = (): HiringPipeline =>
  ({
    id: 'pip-001',
    applicationId: 'app-001',
    jobId: 'job-001',
    candidateId: 'cand-001',
    employerId: 'emp-001',
  }) as HiringPipeline;

describe('InterviewService', () => {
  let service: InterviewService;
  let interviewRepo: any;
  let pipelineRepo: any;
  let publisher: any;

  beforeEach(async () => {
    interviewRepo = { findOne: jest.fn(), create: jest.fn(), save: jest.fn(), find: jest.fn() };
    pipelineRepo = { findOne: jest.fn() };
    publisher = { emit: jest.fn() };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        InterviewService,
        { provide: getRepositoryToken(Interview), useValue: interviewRepo },
        { provide: getRepositoryToken(HiringPipeline), useValue: pipelineRepo },
        { provide: ServiceTokens.INTERVIEW_PUBLISHER, useValue: publisher },
      ],
    }).compile();

    service = module.get<InterviewService>(InterviewService);
  });

  afterEach(() => jest.clearAllMocks());

  describe('scheduleInterview', () => {
    const dto = {
      applicationId: 'app-001',
      scheduledAt: '2026-04-01T10:00:00Z',
      durationMinutes: 60,
      format: InterviewFormat.ONLINE,
    };

    it('should throw NotFoundException when pipeline not found', async () => {
      pipelineRepo.findOne.mockResolvedValue(null);
      await expect(service.scheduleInterview(dto as any, 'emp-001')).rejects.toThrow(NotFoundException);
    });

    it('should create interview from pipeline data and emit event', async () => {
      const pipeline = mockPipeline();
      const interview = mockInterview();
      pipelineRepo.findOne.mockResolvedValue(pipeline);
      interviewRepo.create.mockReturnValue(interview);
      interviewRepo.save.mockResolvedValue(interview);

      const result = await service.scheduleInterview(dto as any, 'emp-001');

      expect(interviewRepo.create).toHaveBeenCalledWith(expect.objectContaining({
        jobId: pipeline.jobId,
        candidateId: pipeline.candidateId,
        interviewerId: 'emp-001',
      }));
      expect(publisher.emit).toHaveBeenCalled();
      expect(result).toEqual(interview);
    });
  });

  describe('updateInterview', () => {
    it('should throw NotFoundException when interview not found', async () => {
      interviewRepo.findOne.mockResolvedValue(null);
      await expect(service.updateInterview('int-001', {}, 'emp-001')).rejects.toThrow(NotFoundException);
    });

    it('should throw ForbiddenException when user is not interviewer or candidate', async () => {
      interviewRepo.findOne.mockResolvedValue(mockInterview());
      await expect(service.updateInterview('int-001', {}, 'other-user')).rejects.toThrow(ForbiddenException);
    });

    it('should update interview and emit event when interviewer', async () => {
      const interview = mockInterview();
      interviewRepo.findOne.mockResolvedValue(interview);
      interviewRepo.save.mockResolvedValue(interview);

      await service.updateInterview('int-001', { notes: 'updated' }, 'emp-001');
      expect(publisher.emit).toHaveBeenCalled();
    });

    it('should allow candidate to update interview', async () => {
      const interview = mockInterview();
      interviewRepo.findOne.mockResolvedValue(interview);
      interviewRepo.save.mockResolvedValue(interview);

      await service.updateInterview('int-001', { notes: 'updated' }, 'cand-001');
      expect(publisher.emit).toHaveBeenCalled();
    });
  });

  describe('cancelInterview', () => {
    it('should throw NotFoundException when interview not found', async () => {
      interviewRepo.findOne.mockResolvedValue(null);
      await expect(service.cancelInterview('int-001', 'emp-001')).rejects.toThrow(NotFoundException);
    });

    it('should throw ForbiddenException when non-interviewer tries to cancel', async () => {
      interviewRepo.findOne.mockResolvedValue(mockInterview());
      await expect(service.cancelInterview('int-001', 'other-user')).rejects.toThrow(ForbiddenException);
    });

    it('should cancel interview and emit event', async () => {
      const interview = mockInterview();
      interviewRepo.findOne.mockResolvedValue(interview);
      interviewRepo.save.mockResolvedValue({ ...interview, status: InterviewStatus.CANCELLED });

      const result = await service.cancelInterview('int-001', 'emp-001');
      expect(interview.status).toBe(InterviewStatus.CANCELLED);
      expect(publisher.emit).toHaveBeenCalled();
      expect(result.status).toBe(InterviewStatus.CANCELLED);
    });
  });

  describe('acceptInterview', () => {
    it('should throw ForbiddenException when wrong candidate', async () => {
      interviewRepo.findOne.mockResolvedValue(mockInterview());
      await expect(service.acceptInterview('int-001', 'wrong-cand')).rejects.toThrow(ForbiddenException);
    });

    it('should accept interview for correct candidate', async () => {
      const interview = mockInterview();
      interviewRepo.findOne.mockResolvedValue(interview);
      interviewRepo.save.mockResolvedValue({ ...interview, status: InterviewStatus.ACCEPTED });

      const result = await service.acceptInterview('int-001', 'cand-001');
      expect(result.status).toBe(InterviewStatus.ACCEPTED);
    });
  });

  describe('declineInterview', () => {
    it('should throw ForbiddenException when wrong candidate', async () => {
      interviewRepo.findOne.mockResolvedValue(mockInterview());
      await expect(service.declineInterview('int-001', 'wrong-cand')).rejects.toThrow(ForbiddenException);
    });

    it('should decline interview for correct candidate', async () => {
      const interview = mockInterview();
      interviewRepo.findOne.mockResolvedValue(interview);
      interviewRepo.save.mockResolvedValue({ ...interview, status: InterviewStatus.DECLINED });

      const result = await service.declineInterview('int-001', 'cand-001');
      expect(result.status).toBe(InterviewStatus.DECLINED);
    });
  });

  describe('getByEmployer', () => {
    it('should return interviews for employer', async () => {
      const interviews = [mockInterview()];
      interviewRepo.find.mockResolvedValue(interviews);
      const result = await service.getByEmployer('emp-001');
      expect(result).toEqual(interviews);
    });
  });

  describe('getByCandidate', () => {
    it('should return interviews for candidate', async () => {
      const interviews = [mockInterview()];
      interviewRepo.find.mockResolvedValue(interviews);
      const result = await service.getByCandidate('cand-001');
      expect(result).toEqual(interviews);
    });
  });
});
