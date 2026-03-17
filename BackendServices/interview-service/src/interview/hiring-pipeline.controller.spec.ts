import { Test, TestingModule } from '@nestjs/testing';
import { HiringPipelineController } from './hiring-pipeline.controller';
import { HiringPipelineService } from './hiring-pipeline.service';
import { AuthGuard } from '../auth/auth.guard';
import { HiringStage } from '../constants';

const mockReq = { user: { userId: 'user-001', role: 'EMPLOYER' } };

describe('HiringPipelineController', () => {
  let controller: HiringPipelineController;
  let service: jest.Mocked<HiringPipelineService>;

  beforeEach(async () => {
    const mockService = {
      createPipeline: jest.fn(),
      advanceStage: jest.fn(),
      getByEmployer: jest.fn(),
      getByCandidate: jest.fn(),
    };

    const module: TestingModule = await Test.createTestingModule({
      controllers: [HiringPipelineController],
      providers: [{ provide: HiringPipelineService, useValue: mockService }],
    })
      .overrideGuard(AuthGuard)
      .useValue({ canActivate: () => true })
      .compile();

    controller = module.get<HiringPipelineController>(HiringPipelineController);
    service = module.get(HiringPipelineService);
  });

  afterEach(() => jest.clearAllMocks());

  it('create — delegates to createPipeline', () => {
    const dto = { applicationId: 'app-001', jobId: 'job-001', candidateId: 'cand-001', employerId: 'emp-001' };
    controller.create(dto);
    expect(service.createPipeline).toHaveBeenCalledWith(dto);
  });

  it('advanceStage — delegates to advanceStage with id and dto', () => {
    const dto = { stage: HiringStage.SCREENING, notes: 'Moving' };
    controller.advanceStage('pip-001', dto, mockReq);
    expect(service.advanceStage).toHaveBeenCalledWith('pip-001', dto);
  });

  it('getByEmployer — delegates to getByEmployer with userId', () => {
    controller.getByEmployer(mockReq);
    expect(service.getByEmployer).toHaveBeenCalledWith('user-001');
  });

  it('getByCandidate — delegates to getByCandidate with userId', () => {
    controller.getByCandidate(mockReq);
    expect(service.getByCandidate).toHaveBeenCalledWith('user-001');
  });
});
