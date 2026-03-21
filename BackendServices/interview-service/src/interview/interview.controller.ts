import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Patch,
  Post,
  Request,
  UseGuards,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import { AuthGuard } from '../auth/auth.guard';
import { InterviewService } from './interview.service';
import { CreateInterviewDto } from './dto/create-interview.dto';
import { UpdateInterviewDto } from './dto/update-interview.dto';

@ApiTags('Interviews')
@ApiBearerAuth()
@UseGuards(AuthGuard)
@Controller('api/interviews')
export class InterviewController {
  constructor(private readonly interviewService: InterviewService) {}

  @Post()
  @ApiOperation({ summary: 'Schedule an interview (Employer)' })
  @ApiResponse({ status: 201, description: 'Interview scheduled' })
  schedule(@Body() dto: CreateInterviewDto, @Request() req) {
    return this.interviewService.scheduleInterview(dto, req.user.userId);
  }

  @Patch(':id')
  @ApiOperation({ summary: 'Update an interview (Employer)' })
  update(@Param('id') id: string, @Body() dto: UpdateInterviewDto, @Request() req) {
    return this.interviewService.updateInterview(id, dto, req.user.userId);
  }

  @Delete(':id')
  @ApiOperation({ summary: 'Cancel an interview (Employer)' })
  cancel(@Param('id') id: string, @Request() req) {
    return this.interviewService.cancelInterview(id, req.user.userId);
  }

  @Get('employer')
  @ApiOperation({ summary: 'List interviews for the logged-in employer' })
  getByEmployer(@Request() req) {
    return this.interviewService.getByEmployer(req.user.userId);
  }

  @Get('candidate')
  @ApiOperation({ summary: 'List interviews for the logged-in candidate' })
  getByCandidate(@Request() req) {
    return this.interviewService.getByCandidate(req.user.userId);
  }

  @Patch(':id/accept')
  @ApiOperation({ summary: 'Accept an interview slot (Candidate)' })
  accept(@Param('id') id: string, @Request() req) {
    return this.interviewService.acceptInterview(id, req.user.userId);
  }

  @Patch(':id/decline')
  @ApiOperation({ summary: 'Decline an interview slot (Candidate)' })
  decline(@Param('id') id: string, @Request() req) {
    return this.interviewService.declineInterview(id, req.user.userId);
  }
}
