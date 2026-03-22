import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { HttpModule } from '@nestjs/axios';
import { JobsModule } from './jobs/jobs.module';
import { ApplicationsModule } from './applications/applications.module';
import { CompanyClientModule } from './company-client/company-client.module';
import { Job } from './jobs/entities/job.entity';
import { Application } from './applications/entities/application.entity';
import { MessagingModule } from './messaging/messaging.module';

@Module({
  imports: [
    // Config
    ConfigModule.forRoot({ isGlobal: true }),

    // Database
    TypeOrmModule.forRootAsync({
      imports: [ConfigModule],
      inject: [ConfigService],
      useFactory: (config: ConfigService) => ({
        type: 'postgres',
        url: config.get<string>('DATABASE_URL'),
        entities: [Job, Application],
        synchronize: true, // Auto-sync schema in dev — disable in production
        logging: config.get('NODE_ENV') === 'development',
      }),
    }),

    // HTTP client (for calling Company Service)
    HttpModule,

    // Feature modules
    MessagingModule,
    JobsModule,
    ApplicationsModule,
    CompanyClientModule,
  ],
})
export class AppModule {}
