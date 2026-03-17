import { Test, TestingModule } from '@nestjs/testing';
import { InterviewController } from './interview.controller';
import { InterviewService } from './interview.service';
import { AuthGuard } from '../auth/auth.guard';
import { InterviewFormat } from '../constants';

const mockReq = { user: { userId: 'user-001', role: 'EMPLOYER' } };

describe('InterviewController', () => {
  let controller: InterviewController;
  let service: jest.Mocked<InterviewService>;

  beforeEach(async () => {
    const mockService = {
      scheduleInterview: jest.fn(),
      updateInterview: jest.fn(),
      cancelInterview: jest.fn(),
      getByEmployer: jest.fn(),
      getByCandidate: jest.fn(),
      acceptInterview: jest.fn(),
      declineInterview: jest.fn(),
    };

    const module: TestingModule = await Test.createTestingModule({
      controllers: [InterviewController],
      providers: [{ provide: InterviewService, useValue: mockService }],
    })
      .overrideGuard(AuthGuard)
      .useValue({ canActivate: () => true })
      .compile();

    controller = module.get<InterviewController>(InterviewController);
    service = module.get(InterviewService);
  });

  afterEach(() => jest.clearAllMocks());

  it('schedule — delegates to scheduleInterview with userId', () => {
    const dto = { applicationId: 'app-001', scheduledAt: '2026-04-01T10:00:00Z', durationMinutes: 60, format: InterviewFormat.ONLINE };
    controller.schedule(dto as any, mockReq);
    expect(service.scheduleInterview).toHaveBeenCalledWith(dto, 'user-001');
  });

  it('update — delegates to updateInterview with id and userId', () => {
    controller.update('int-001', { notes: 'updated' } as any, mockReq);
    expect(service.updateInterview).toHaveBeenCalledWith('int-001', { notes: 'updated' }, 'user-001');
  });

  it('cancel — delegates to cancelInterview', () => {
    controller.cancel('int-001', mockReq);
    expect(service.cancelInterview).toHaveBeenCalledWith('int-001', 'user-001');
  });

  it('getByEmployer — delegates to getByEmployer with userId', () => {
    controller.getByEmployer(mockReq);
    expect(service.getByEmployer).toHaveBeenCalledWith('user-001');
  });

  it('getByCandidate — delegates to getByCandidate with userId', () => {
    controller.getByCandidate(mockReq);
    expect(service.getByCandidate).toHaveBeenCalledWith('user-001');
  });

  it('accept — delegates to acceptInterview', () => {
    controller.accept('int-001', mockReq);
    expect(service.acceptInterview).toHaveBeenCalledWith('int-001', 'user-001');
  });

  it('decline — delegates to declineInterview', () => {
    controller.decline('int-001', mockReq);
    expect(service.declineInterview).toHaveBeenCalledWith('int-001', 'user-001');
  });
});
