import { Inject, Injectable } from '@nestjs/common';
import { ClientProxy } from '@nestjs/microservices';
import { firstValueFrom } from 'rxjs';
import { AuthEvents, ServiceTokens } from '../constants';

@Injectable()
export class AuthService {
  constructor(
    @Inject(ServiceTokens.AUTH_SERVICE) private readonly client: ClientProxy,
  ) {}

  async validateToken(token: string): Promise<any> {
    return firstValueFrom(this.client.send(AuthEvents.AUTH_VALIDATE, token));
  }
}
