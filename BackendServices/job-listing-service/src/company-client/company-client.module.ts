import { Module } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { ConfigModule } from '@nestjs/config';
import { CompanyClientService } from './company-client.service';

@Module({
  imports: [HttpModule, ConfigModule],
  providers: [CompanyClientService],
  exports: [CompanyClientService],
})
export class CompanyClientModule {}
