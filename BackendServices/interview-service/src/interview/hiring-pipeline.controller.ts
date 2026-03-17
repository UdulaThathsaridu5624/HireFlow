import { Body, Controller, Get, Param, Patch, Post, Request, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiResponse, ApiTags } from '@nestjs/swagger';
import { AuthGuard } from '../auth/auth.guard';
import { HiringPipelineService } from './hiring-pipeline.service';
import { CreatePipelineDto } from './dto/create-pipeline.dto';
import { UpdateStageDto } from './dto/update-stage.dto';

@ApiTags('Hiring Pipelines')
@ApiBearerAuth()
@UseGuards(AuthGuard)
@Controller('api/pipelines')
export class HiringPipelineController {
  constructor(private readonly pipelineService: HiringPipelineService) {}

  @Post()
  @ApiOperation({ summary: 'Create a hiring pipeline for an application' })
  @ApiResponse({ status: 201, description: 'Pipeline created' })
  create(@Body() dto: CreatePipelineDto) {
    return this.pipelineService.createPipeline(dto);
  }

  @Patch(':id/stage')
  @ApiOperation({ summary: 'Advance candidate to next hiring stage (Employer)' })
  advanceStage(@Param('id') id: string, @Body() dto: UpdateStageDto, @Request() _req) {
    return this.pipelineService.advanceStage(id, dto);
  }

  @Get('employer')
  @ApiOperation({ summary: 'List all pipelines for the logged-in employer' })
  getByEmployer(@Request() req) {
    return this.pipelineService.getByEmployer(req.user.userId);
  }

  @Get('candidate')
  @ApiOperation({ summary: 'List all pipelines for the logged-in candidate' })
  getByCandidate(@Request() req) {
    return this.pipelineService.getByCandidate(req.user.userId);
  }
}
