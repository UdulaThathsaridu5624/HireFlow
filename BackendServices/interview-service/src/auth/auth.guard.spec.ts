import { ExecutionContext, UnauthorizedException } from '@nestjs/common';
import { Test, TestingModule } from '@nestjs/testing';
import { AuthGuard } from './auth.guard';
import { AuthService } from './auth.service';

const mockExecutionContext = (authHeader?: string): ExecutionContext =>
  ({
    switchToHttp: () => ({
      getRequest: () => ({
        headers: { authorization: authHeader },
        user: undefined as any,
      }),
    }),
  }) as unknown as ExecutionContext;

describe('AuthGuard', () => {
  let guard: AuthGuard;
  let authService: jest.Mocked<AuthService>;

  beforeEach(async () => {
    const mockAuthService = { validateToken: jest.fn() };
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        AuthGuard,
        { provide: AuthService, useValue: mockAuthService },
      ],
    }).compile();

    guard = module.get<AuthGuard>(AuthGuard);
    authService = module.get(AuthService);
  });

  afterEach(() => jest.clearAllMocks());

  it('should throw when no authorization header', async () => {
    await expect(guard.canActivate(mockExecutionContext(undefined))).rejects.toThrow(
      new UnauthorizedException('No token provided'),
    );
  });

  it('should throw when header does not start with Bearer', async () => {
    await expect(guard.canActivate(mockExecutionContext('Basic abc'))).rejects.toThrow(
      new UnauthorizedException('No token provided'),
    );
  });

  it('should throw when token is invalid', async () => {
    authService.validateToken.mockResolvedValue({ valid: false });
    await expect(guard.canActivate(mockExecutionContext('Bearer bad-token'))).rejects.toThrow(
      new UnauthorizedException('Invalid or expired token'),
    );
  });

  it('should return true and set request.user when token is valid', async () => {
    authService.validateToken.mockResolvedValue({ valid: true, userId: '123', role: 'EMPLOYER' });
    const ctx = mockExecutionContext('Bearer valid-token');
    const result = await guard.canActivate(ctx);
    expect(result).toBe(true);
    expect(authService.validateToken).toHaveBeenCalledWith('valid-token');
  });
});
