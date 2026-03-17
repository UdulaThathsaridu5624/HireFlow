import 'dotenv/config';
import { NestFactory } from '@nestjs/core';
import { ValidationPipe } from '@nestjs/common';
import { AppModule } from './app.module';
import { MicroserviceOptions, Transport } from '@nestjs/microservices';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  app.enableCors();
  app.useGlobalPipes(new ValidationPipe({ whitelist: true, transform: true }));

  const config = new DocumentBuilder()
    .setTitle('Interview & Hiring Pipeline Service')
    .setDescription('API for scheduling interviews and managing hiring pipelines')
    .setVersion('1.0')
    .addBearerAuth()
    .build();
  const document = SwaggerModule.createDocument(app, config);
  SwaggerModule.setup('api', app, document);

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
  console.log('[Interview Service] Swagger UI at http://localhost:3000/api');
  console.log('[Interview Service] RabbitMQ connected to hireflow_events_queue');
}
bootstrap(); // NOSONAR typescript:S7785 — top-level await unavailable in CJS context
