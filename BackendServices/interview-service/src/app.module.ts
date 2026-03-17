import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { AuthService } from './auth/auth.service';
import { InterviewModule } from './interview/interview.module';
import { Interview } from './interview/entities/interview.entity';
import { HiringPipeline } from './interview/entities/hiring-pipeline.entity';
import { Queues, ServiceTokens } from './constants';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    TypeOrmModule.forRoot({
      type: 'postgres',
      url: process.env.DATABASE_URL,
      entities: [Interview, HiringPipeline],
      synchronize: true,
    }),
    ClientsModule.register([
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
    InterviewModule,
  ],
  controllers: [AppController],
  providers: [AppService, AuthService],
})
export class AppModule {}
