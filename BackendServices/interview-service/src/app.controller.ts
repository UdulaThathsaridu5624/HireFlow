import { Controller, Get, Headers, UnauthorizedException } from '@nestjs/common';
import { EventPattern, Payload } from '@nestjs/microservices';
import { AppService } from './app.service';
import { AuthService } from './auth/auth.service';
import { AuthEvents } from './constants';

@Controller()
export class AppController {
  constructor(
    private readonly appService: AppService,
    private readonly authService: AuthService,
  ) {}

  @Get()
  getHello(): string {
    return this.appService.getHello();
  }

  @Get('protected')
  async getProtected(@Headers('authorization') authHeader: string) {
    if (!authHeader?.startsWith('Bearer ')) {
      throw new UnauthorizedException('No token provided');
    }
    const token = authHeader.substring(7);
    const result = await this.authService.validateToken(token);
    if (!result.valid) {
      throw new UnauthorizedException('Invalid token');
    }
    return { message: 'Access granted', user: result };
  }

  @EventPattern(AuthEvents.USER_REGISTERED)
  handleUserRegistered(@Payload() data: any) {
    console.log('[Interview Service] User registered:', data);
  }

  @EventPattern(AuthEvents.USER_LOGGED_IN)
  handleUserLoggedIn(@Payload() data: any) {
    console.log('[Interview Service] User logged in:', data);
  }
}
