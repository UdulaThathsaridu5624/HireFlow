import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { ClientsModule, Transport } from '@nestjs/microservices';
import { AuthService } from './auth/auth.service';
import { Queues, ServiceTokens } from './constants';

@Module({
  imports: [
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
  ],
  controllers: [AppController],
  providers: [AppService, AuthService],
})
export class AppModule {}
