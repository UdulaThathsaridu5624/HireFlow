import { Controller, Get, Headers, Logger, UnauthorizedException } from '@nestjs/common';
import { EventPattern, Payload } from '@nestjs/microservices';
import { AppService } from './app.service';
import { AuthService } from './auth/auth.service';
import { AuthEvents, InterviewEvents } from './constants';
import { HiringPipelineService } from './interview/hiring-pipeline.service';

@Controller()
export class AppController {
  private readonly logger = new Logger(AppController.name);

  constructor(
    private readonly appService: AppService,
    private readonly authService: AuthService,
    private readonly pipelineService: HiringPipelineService,
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
    this.logger.log(`User registered: ${JSON.stringify(data)}`);
  }

  @EventPattern(AuthEvents.USER_LOGGED_IN)
  handleUserLoggedIn(@Payload() data: any) {
    this.logger.log(`User logged in: ${JSON.stringify(data)}`);
  }

  @EventPattern(InterviewEvents.APPLICATION_FORWARDED)
  async handleApplicationForwarded(@Payload() data: any) {
    this.logger.log(`Application forwarded, creating pipeline: ${JSON.stringify(data)}`);
    await this.pipelineService.createPipeline({
      applicationId: data.applicationId,
      jobId: data.jobId,
      candidateId: data.candidateId,
      employerId: data.employerId,
    });
  }
}
