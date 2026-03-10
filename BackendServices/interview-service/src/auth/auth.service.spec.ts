import { Test, TestingModule } from '@nestjs/testing';
import { of } from 'rxjs';
import { AuthService } from './auth.service';
import { ServiceTokens, AuthEvents } from '../constants';

describe('AuthService', () => {
  let authService: AuthService;
  const mockClient = {
    send: jest.fn(),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        AuthService,
        { provide: ServiceTokens.AUTH_SERVICE, useValue: mockClient },
      ],
    }).compile();

    authService = module.get<AuthService>(AuthService);
  });

  afterEach(() => jest.clearAllMocks());

  describe('validateToken', () => {
    it('should send token to auth service and return result', async () => {
      const mockResult = { valid: true, userId: '123' };
      mockClient.send.mockReturnValue(of(mockResult));

      const result = await authService.validateToken('test-token');

      expect(mockClient.send).toHaveBeenCalledWith(AuthEvents.AUTH_VALIDATE, 'test-token');
      expect(result).toEqual(mockResult);
    });

    it('should return invalid result when token is rejected', async () => {
      const mockResult = { valid: false };
      mockClient.send.mockReturnValue(of(mockResult));

      const result = await authService.validateToken('bad-token');

      expect(result).toEqual(mockResult);
    });
  });
});
