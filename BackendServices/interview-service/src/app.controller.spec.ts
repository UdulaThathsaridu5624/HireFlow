import { Test, TestingModule } from '@nestjs/testing';
import { UnauthorizedException } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { AuthService } from './auth/auth.service';

describe('AppController', () => {
  let appController: AppController;
  let authService: jest.Mocked<AuthService>;

  beforeEach(async () => {
    const mockAuthService = {
      validateToken: jest.fn(),
    };

    const app: TestingModule = await Test.createTestingModule({
      controllers: [AppController],
      providers: [
        AppService,
        { provide: AuthService, useValue: mockAuthService },
      ],
    }).compile();

    appController = app.get<AppController>(AppController);
    authService = app.get(AuthService);
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
    it('should log user registered event', () => {
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();
      const data = { userId: '1', email: 'test@test.com' };
      appController.handleUserRegistered(data);
      expect(consoleSpy).toHaveBeenCalledWith('[Interview Service] User registered:', data);
      consoleSpy.mockRestore();
    });
  });

  describe('handleUserLoggedIn', () => {
    it('should log user logged in event', () => {
      const consoleSpy = jest.spyOn(console, 'log').mockImplementation();
      const data = { userId: '1' };
      appController.handleUserLoggedIn(data);
      expect(consoleSpy).toHaveBeenCalledWith('[Interview Service] User logged in:', data);
      consoleSpy.mockRestore();
    });
  });
});
