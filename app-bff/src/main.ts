import { NestFactory } from '@nestjs/core';
import express from 'express';
import { join } from 'path';
import { AppModule } from './app.module';

async function bootstrap() {
  const allowedOrigins = resolveAllowedCorsOrigins();
  const app = await NestFactory.create(AppModule, {
    cors: {
      origin: (origin, callback) => {
        if (!origin || allowedOrigins.includes(origin)) {
          callback(null, true);
          return;
        }
        callback(new Error('CORS origin not allowed'));
      },
      credentials: true,
      methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
      allowedHeaders: ['Content-Type', 'Authorization', 'x-request-id', 'x-device-id', 'x-legacy-device-ids'],
      exposedHeaders: ['x-request-id'],
    },
  });

  app.setGlobalPrefix('');
  app.getHttpAdapter().getInstance().use('/demo-avatar', express.static(join(process.cwd(), 'public', 'demo-avatar')));
  await app.listen(process.env.PORT ?? 3000);
}

bootstrap();

function resolveAllowedCorsOrigins(): string[] {
  const raw = process.env.BFF_CORS_ALLOW_ORIGINS
    ?? 'http://127.0.0.1:3000,http://localhost:3000,http://127.0.0.1:8080,http://localhost:8080';
  return raw
    .split(',')
    .map((item) => item.trim())
    .filter((item) => item.length > 0);
}
