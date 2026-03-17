import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { Interview } from './entities/interview.entity';
import { HiringPipeline } from './entities/hiring-pipeline.entity';
import { InterviewService } from './interview.service';
import { HiringPipelineService } from './hiring-pipeline.service';
import { InterviewController } from './interview.controller';
import { HiringPipelineController } from './hiring-pipeline.controller';
import { AuthService } from '../auth/auth.service';
import { AuthGuard } from '../auth/auth.guard';
import { Queues, ServiceTokens } from '../constants';

@Module({
  imports: [
    TypeOrmModule.forFeature([Interview, HiringPipeline]),
    ClientsModule.register([
      {
        name: ServiceTokens.INTERVIEW_PUBLISHER,
        transport: Transport.RMQ,
        options: {
          urls: [process.env.RABBITMQ_URL!],
          queue: Queues.HIREFLOW_EVENTS,
          queueOptions: { durable: true },
        },
      },
      {
        name: ServiceTokens.AUTH_SERVICE,
        transport: Transport.RMQ,
        options: {
          urls: [process.env.RABBITMQ_URL!],
          queue: Queues.AUTH_VALIDATE,
          queueOptions: { durable: true },
        },
      },
    ]),
  ],
  controllers: [InterviewController, HiringPipelineController],
  providers: [InterviewService, HiringPipelineService, AuthService, AuthGuard],
  exports: [HiringPipelineService],
})
export class InterviewModule {}
