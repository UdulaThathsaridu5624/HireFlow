import { Injectable, Logger, NotFoundException } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom } from 'rxjs';

export interface CompanyProfile {
  id: string;
  name: string;
  industry: string;
  location: string;
  website?: string;
  description?: string;
}

@Injectable()
export class CompanyClientService {
  private readonly logger = new Logger(CompanyClientService.name);
  private readonly companyServiceUrl: string;

  constructor(
    private readonly httpService: HttpService,
    private readonly configService: ConfigService,
  ) {
    this.companyServiceUrl = this.configService.get<string>('COMPANY_SERVICE_URL');
  }

  /**
   * Fetch a company profile by employerId from Tharindu's Company Service.
   * Called when attaching company info to job listings.
   */
  async getCompanyByEmployerId(employerId: string, token: string): Promise<CompanyProfile | null> {
    try {
      const response = await firstValueFrom(
        this.httpService.get<CompanyProfile>(
          `${this.companyServiceUrl}/companies/employer/${employerId}`,
          {
            headers: { Authorization: `Bearer ${token}` },
            timeout: 5000,
          },
        ),
      );
      return response.data;
    } catch (error) {
      this.logger.warn(`Could not fetch company for employer ${employerId}: ${error.message}`);
      return null;
    }
  }

  /**
   * Fetch a company profile by companyId.
   * Called when a candidate views a job listing's full details.
   */
  async getCompanyById(companyId: string, token: string): Promise<CompanyProfile | null> {
    try {
      const response = await firstValueFrom(
        this.httpService.get<CompanyProfile>(
          `${this.companyServiceUrl}/companies/${companyId}`,
          {
            headers: { Authorization: `Bearer ${token}` },
            timeout: 5000,
          },
        ),
      );
      return response.data;
    } catch (error) {
      this.logger.warn(`Could not fetch company ${companyId}: ${error.message}`);
      return null;
    }
  }
}
