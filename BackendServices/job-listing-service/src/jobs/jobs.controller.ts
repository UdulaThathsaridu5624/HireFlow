import {
  Controller,
  Get,
  Post,
  Patch,
  Delete,
  Body,
  Param,
  Query,
  UseGuards,
  Request,
  ParseUUIDPipe,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import {
  ApiTags,
  ApiBearerAuth,
  ApiOperation,
  ApiResponse,
  ApiParam,
} from '@nestjs/swagger';
import { JobsService } from './jobs.service';
import { CreateJobDto } from './dto/create-job.dto';
import { UpdateJobDto } from './dto/update-job.dto';
import { SearchJobsDto } from './dto/search-jobs.dto';
import { JwtAuthGuard } from '../common/guards/jwt-auth.guard';
import { RolesGuard } from '../common/guards/roles.guard';
import { Roles } from '../common/decorators/roles.decorator';
import { Public } from '../common/decorators/public.decorator';
import { CurrentUser } from '../common/decorators/current-user.decorator';
import { UserRole } from '../constants';

@ApiTags('Jobs')
@ApiBearerAuth('JWT')
@UseGuards(JwtAuthGuard, RolesGuard)
@Controller('jobs')
export class JobsController {
  constructor(private readonly jobsService: JobsService) {}

  // ─────────────────────────────────────────────
  // EMPLOYER ENDPOINTS
  // ─────────────────────────────────────────────

  @Post()
  @Roles(UserRole.EMPLOYER)
  @ApiOperation({ summary: 'Employer: Post a new job listing' })
  @ApiResponse({ status: 201, description: 'Job created successfully' })
  async create(
    @Body() dto: CreateJobDto,
    @CurrentUser('id') employerId: string,
    @Request() req,
  ) {
    const token = req.headers.authorization?.split(' ')[1];
    return this.jobsService.create(dto, employerId, token);
  }

  @Patch(':id')
  @Roles(UserRole.EMPLOYER)
  @ApiOperation({ summary: 'Employer: Edit a job listing' })
  @ApiParam({ name: 'id', description: 'Job UUID' })
  async update(
    @Param('id', ParseUUIDPipe) id: string,
    @Body() dto: UpdateJobDto,
    @CurrentUser('id') employerId: string,
  ) {
    return this.jobsService.update(id, dto, employerId);
  }

  @Patch(':id/close')
  @Roles(UserRole.EMPLOYER)
  @ApiOperation({ summary: 'Employer: Close a job posting' })
  @ApiParam({ name: 'id', description: 'Job UUID' })
  async closeJob(
    @Param('id', ParseUUIDPipe) id: string,
    @CurrentUser('id') employerId: string,
  ) {
    return this.jobsService.closeJob(id, employerId);
  }

  @Get('my-jobs')
  @Roles(UserRole.EMPLOYER)
  @ApiOperation({ summary: 'Employer: View all their job postings' })
  async getMyJobs(@CurrentUser('id') employerId: string, @Request() req) {
    const token = req.headers.authorization?.split(' ')[1];
    return this.jobsService.findAllByEmployer(employerId, token);
  }

  // ─────────────────────────────────────────────
  // CANDIDATE ENDPOINTS
  // ─────────────────────────────────────────────

  @Get()
  @Public()
  @ApiOperation({ summary: 'Candidate: Browse all open job listings' })
  async findAllOpen(@Request() req) {
    const token = req.headers.authorization?.split(' ')[1] ?? '';
    return this.jobsService.findAllOpen(token);
  }

  @Get('search')
  @Public()
  @ApiOperation({ summary: 'Candidate: Search and filter jobs' })
  async search(@Query() filters: SearchJobsDto, @Request() req) {
    const token = req.headers.authorization?.split(' ')[1] ?? '';
    return this.jobsService.search(filters, token);
  }

  @Get(':id')
  @Public()
  @ApiOperation({ summary: 'Candidate: View full job details with company info' })
  @ApiParam({ name: 'id', description: 'Job UUID' })
  async findOne(@Param('id', ParseUUIDPipe) id: string, @Request() req) {
    const token = req.headers.authorization?.split(' ')[1] ?? '';
    return this.jobsService.findOneWithCompany(id, token);
  }

  // ─────────────────────────────────────────────
  // INTERNAL ENDPOINT (called by Madhini's Application Service)
  // ─────────────────────────────────────────────

  @Get(':id/verify')
  @ApiOperation({ summary: 'Internal: Verify if a job is open (used by Application Service)' })
  @ApiParam({ name: 'id', description: 'Job UUID' })
  async verifyJob(@Param('id', ParseUUIDPipe) id: string) {
    return this.jobsService.verifyJobIsOpen(id);
  }
}
