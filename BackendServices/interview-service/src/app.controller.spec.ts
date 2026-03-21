import { Test, TestingModule } from '@nestjs/testing';
import { UnauthorizedException } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { AuthService } from './auth/auth.service';
import { HiringPipelineService } from './interview/hiring-pipeline.service';

describe('AppController', () => {
  let appController: AppController;
  let authService: jest.Mocked<AuthService>;
  let pipelineService: jest.Mocked<HiringPipelineService>;

  beforeEach(async () => {
    const mockAuthService = {
      validateToken: jest.fn(),
    };

    const mockPipelineService = {
      createPipeline: jest.fn(),
    };

    const app: TestingModule = await Test.createTestingModule({
      controllers: [AppController],
      providers: [
        AppService,
        { provide: AuthService, useValue: mockAuthService },
        { provide: HiringPipelineService, useValue: mockPipelineService },
      ],
    }).compile();

    appController = app.get<AppController>(AppController);
    authService = app.get(AuthService);
    pipelineService = app.get(HiringPipelineService);
  });

  describe('getHello', () => {
    it('should return "Hello World!"', () => {
      expect(appController.getHello()).toBe('Hello World!');
    });
  });

  describe('getProtected', () => {
    it('should throw UnauthorizedException when no auth header', async () => {
      await expect(appController.getProtected(undefined as any)).rejects.toThrow(
        new UnauthorizedException('No token provided'),
      );
    });

    it('should throw UnauthorizedException when header does not start with Bearer', async () => {
      await expect(appController.getProtected('Basic abc123')).rejects.toThrow(
        new UnauthorizedException('No token provided'),
      );
    });

    it('should throw UnauthorizedException when token is invalid', async () => {
      authService.validateToken.mockResolvedValue({ valid: false });
      await expect(appController.getProtected('Bearer bad-token')).rejects.toThrow(
        new UnauthorizedException('Invalid token'),
      );
    });

    it('should return user data when token is valid', async () => {
      const mockUser = { valid: true, userId: '123' };
      authService.validateToken.mockResolvedValue(mockUser);
      const result = await appController.getProtected('Bearer valid-token');
      expect(authService.validateToken).toHaveBeenCalledWith('valid-token');
      expect(result).toEqual({ message: 'Access granted', user: mockUser });
    });
  });

  describe('handleUserRegistered', () => {
    it('should handle user registered event', () => {
      const data = { userId: '1', email: 'test@test.com' };
      expect(() => appController.handleUserRegistered(data)).not.toThrow();
    });
  });

  describe('handleUserLoggedIn', () => {
    it('should handle user logged in event', () => {
      const data = { userId: '1' };
      expect(() => appController.handleUserLoggedIn(data)).not.toThrow();
    });
  });

  describe('handleApplicationForwarded', () => {
    it('should create pipeline when application is forwarded', async () => {
      const data = {
        applicationId: 'app-001',
        jobId: 'job-001',
        candidateId: 'cand-001',
        employerId: 'emp-001',
      };
      pipelineService.createPipeline.mockResolvedValue({} as any);
      await appController.handleApplicationForwarded(data);
      expect(pipelineService.createPipeline).toHaveBeenCalledWith({
        applicationId: data.applicationId,
        jobId: data.jobId,
        candidateId: data.candidateId,
        employerId: data.employerId,
      });
    });
  });
});
