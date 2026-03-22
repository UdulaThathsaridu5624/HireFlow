import { Injectable, Logger } from '@nestjs/common';
import { AmqpConnection } from '@golevelup/nestjs-rabbitmq';

@Injectable()
export class MessagingService {
  private readonly logger = new Logger(MessagingService.name);

  constructor(private readonly amqpConnection: AmqpConnection) {}

  // Called when a job is posted — other services can listen for this
  async publishJobPosted(job: {
    jobId: string;
    employerId: string;
    title: string;
    location: string;
    isRemote: boolean;
  }) {
    try {
      await this.amqpConnection.publish('hireflow.exchange', 'job.posted', {
        pattern: 'job.posted',
        data: {
          ...job,
          timestamp: new Date().toISOString(),
        },
      });
      this.logger.log(`Published job.posted event for jobId: ${job.jobId}`);
    } catch (error) {
      this.logger.error('Failed to publish job.posted event', error);
    }
  }

  // Called when a job is closed
  async publishJobClosed(jobId: string, employerId: string) {
    try {
      await this.amqpConnection.publish('hireflow.exchange', 'job.closed', {
        pattern: 'job.closed',
        data: { jobId, employerId, timestamp: new Date().toISOString() },
      });
      this.logger.log(`Published job.closed event for jobId: ${jobId}`);
    } catch (error) {
      this.logger.error('Failed to publish job.closed event', error);
    }
  }
}