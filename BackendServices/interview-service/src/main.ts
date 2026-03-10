import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
   app.connectMicroservice<MicroserviceOptions>({
    transport: Transport.RMQ,
    options: {
      urls: [process.env.RABBITMQ_URL ?? 'amqp://hireflow:hireflow123@localhost:5672'],
      queue: 'hireflow_events_queue',
      queueOptions: { durable: true },
    },
  });
  await app.startAllMicroservices();
  await app.listen(process.env.PORT ?? 3000);
  console.log('[Interview Service] HTTP running on port 3000');
  console.log('[Interview Service] RabbitMQ connected to hireflow_events_queue');
}
bootstrap();
