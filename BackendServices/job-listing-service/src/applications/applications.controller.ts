import {
  Controller,
  Post,
  Get,
  Patch,
  Body,
  Param,
  UseGuards,
  ParseUUIDPipe,
} from '@nestjs/common';
import {
  ApiTags,
  ApiBearerAuth,
  ApiOperation,
  ApiParam,
  ApiResponse,
} from '@nestjs/swagger';
import { ApplicationsService } from './applications.service';
import { ApplyToJobDto } from './dto/apply-to-job.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { UserRole } from '../constants';

@ApiTags('Applications')
@ApiBearerAuth('JWT')
@UseGuards(JwtAuthGuard, RolesGuard)
@Controller('applications')
export class ApplicationsController {
  constructor(private readonly applicationsService: ApplicationsService) {}

  // ─────────────────────────────────────────────
  // CANDIDATE ENDPOINTS
  // ─────────────────────────────────────────────

  @Post('jobs/:jobId/apply')
  @Roles(UserRole.CANDIDATE)
  @ApiOperation({ summary: 'Candidate: Apply to a job by filling the application form' })
  @ApiParam({ name: 'jobId', description: 'Job UUID' })
  @ApiResponse({ status: 201, description: 'Application submitted successfully' })
  @ApiResponse({ status: 409, description: 'Already applied to this job' })
  @ApiResponse({ status: 400, description: 'Job is closed or not accepting applications' })
  async apply(
    @Param('jobId', ParseUUIDPipe) jobId: string,
    @Body() dto: ApplyToJobDto,
    @CurrentUser('id') candidateId: string,
  ) {
    return this.applicationsService.apply(jobId, dto, candidateId);
  }

  @Get('my-applications')
  @Roles(UserRole.CANDIDATE)
  @ApiOperation({ summary: 'Candidate: View all their applications and statuses' })
  async getMyApplications(@CurrentUser('id') candidateId: string) {
    return this.applicationsService.getMyApplications(candidateId);
  }

  @Patch(':id/withdraw')
  @Roles(UserRole.CANDIDATE)
  @ApiOperation({ summary: 'Candidate: Withdraw a pending application' })
  @ApiParam({ name: 'id', description: 'Application UUID' })
  async withdraw(
    @Param('id', ParseUUIDPipe) id: string,
    @CurrentUser('id') candidateId: string,
  ) {
    return this.applicationsService.withdraw(id, candidateId);
  }

  // ─────────────────────────────────────────────
  // EMPLOYER ENDPOINTS
  // ─────────────────────────────────────────────

  @Get('jobs/:jobId')
  @Roles(UserRole.EMPLOYER)
  @ApiOperation({ summary: 'Employer: View all applications for a job posting' })
  @ApiParam({ name: 'jobId', description: 'Job UUID' })
  async getApplicationsForJob(
    @Param('jobId', ParseUUIDPipe) jobId: string,
    @CurrentUser('id') employerId: string,
  ) {
    return this.applicationsService.getApplicationsForJob(jobId, employerId);
  }

  @Get(':id')
  @Roles(UserRole.EMPLOYER)
  @ApiOperation({ summary: "Employer: View a specific candidate's application and CV" })
  @ApiParam({ name: 'id', description: 'Application UUID' })
  async getApplicationById(
    @Param('id', ParseUUIDPipe) id: string,
    @CurrentUser('id') employerId: string,
  ) {
    return this.applicationsService.getApplicationById(id, employerId);
  }
}
