import { NotFoundException } from '@nestjs/common';
import { Test, TestingModule } from '@nestjs/testing';
import { getRepositoryToken } from '@nestjs/typeorm';
import { HiringPipelineService } from './hiring-pipeline.service';
import { HiringPipeline } from './entities/hiring-pipeline.entity';
import { HiringStage } from '../constants';

const mockPipeline = (overrides = {}): HiringPipeline =>
  ({
    id: 'pip-001',
    applicationId: 'app-001',
    jobId: 'job-001',
    candidateId: 'cand-001',
    employerId: 'emp-001',
    currentStage: HiringStage.APPLIED,
    stageHistory: [{ stage: HiringStage.APPLIED, notes: 'Pipeline created', updatedAt: '2026-01-01' }],
    ...overrides,
  }) as HiringPipeline;

describe('HiringPipelineService', () => {
  let service: HiringPipelineService;
  let repo: any;

  beforeEach(async () => {
    repo = { findOne: jest.fn(), create: jest.fn(), save: jest.fn(), find: jest.fn() };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        HiringPipelineService,
        { provide: getRepositoryToken(HiringPipeline), useValue: repo },
      ],
    }).compile();

    service = module.get<HiringPipelineService>(HiringPipelineService);
  });

  afterEach(() => jest.clearAllMocks());

  describe('createPipeline', () => {
    const dto = { applicationId: 'app-001', jobId: 'job-001', candidateId: 'cand-001', employerId: 'emp-001' };

    it('should return existing pipeline if one already exists', async () => {
      const existing = mockPipeline();
      repo.findOne.mockResolvedValue(existing);
      const result = await service.createPipeline(dto);
      expect(result).toEqual(existing);
      expect(repo.create).not.toHaveBeenCalled();
    });

    it('should create new pipeline with APPLIED stage when none exists', async () => {
      const newPipeline = mockPipeline();
      repo.findOne.mockResolvedValue(null);
      repo.create.mockReturnValue(newPipeline);
      repo.save.mockResolvedValue(newPipeline);

      const result = await service.createPipeline(dto);
      expect(repo.create).toHaveBeenCalledWith(expect.objectContaining({
        currentStage: HiringStage.APPLIED,
      }));
      expect(result.currentStage).toBe(HiringStage.APPLIED);
    });
  });

  describe('advanceStage', () => {
    it('should throw NotFoundException when pipeline not found', async () => {
      repo.findOne.mockResolvedValue(null);
      await expect(service.advanceStage('pip-001', { stage: HiringStage.SCREENING })).rejects.toThrow(NotFoundException);
    });

    it('should update stage and append to stageHistory', async () => {
      const pipeline = mockPipeline();
      repo.findOne.mockResolvedValue(pipeline);
      repo.save.mockResolvedValue({ ...pipeline, currentStage: HiringStage.SCREENING });

      const result = await service.advanceStage('pip-001', { stage: HiringStage.SCREENING, notes: 'Moved' });
      expect(pipeline.currentStage).toBe(HiringStage.SCREENING);
      expect(pipeline.stageHistory).toHaveLength(2);
      expect(result.currentStage).toBe(HiringStage.SCREENING);
    });
  });

  describe('getByEmployer', () => {
    it('should return pipelines for employer', async () => {
      const pipelines = [mockPipeline()];
      repo.find.mockResolvedValue(pipelines);
      const result = await service.getByEmployer('emp-001');
      expect(result).toEqual(pipelines);
      expect(repo.find).toHaveBeenCalledWith({ where: { employerId: 'emp-001' } });
    });
  });

  describe('getByCandidate', () => {
    it('should return pipelines for candidate', async () => {
      const pipelines = [mockPipeline()];
      repo.find.mockResolvedValue(pipelines);
      const result = await service.getByCandidate('cand-001');
      expect(result).toEqual(pipelines);
      expect(repo.find).toHaveBeenCalledWith({ where: { candidateId: 'cand-001' } });
    });
  });
});
