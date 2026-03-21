import { Injectable, Logger } from '@nestjs/common';
import { RabbitSubscribe } from '@golevelup/nestjs-rabbitmq';

interface UserEventData {
  userId: string;
  email: string;
  name: string;
  role: string;
  eventType: string;
  timestamp: string;
}

@Injectable()
export class UserEventConsumer {
  private readonly logger = new Logger(UserEventConsumer.name);

  // Fires every time a new user registers in the auth service
  @RabbitSubscribe({
    exchange: 'hireflow.exchange',
    routingKey: 'user.registered',
    queue: 'job_service_user_registered_queue', // unique queue name for YOUR service
  })
  async handleUserRegistered(msg: { pattern: string; data: UserEventData }) {
    const user = msg.data;
    this.logger.log(
      `Received user.registered event: userId=${user.userId}, role=${user.role}`,
    );

    // You can use this to store employer profiles locally if needed
    // For now just log it — add DB logic here later if required
    if (user.role === 'EMPLOYER') {
      this.logger.log(`New employer registered: ${user.name} (${user.email})`);
    }
  }

  // Fires every time a user logs in
  @RabbitSubscribe({
    exchange: 'hireflow.exchange',
    routingKey: 'user.loggedIn',
    queue: 'job_service_user_loggedin_queue',
  })
  async handleUserLoggedIn(msg: { pattern: string; data: UserEventData }) {
    this.logger.log(`User logged in: ${msg.data.email}`);
  }
}