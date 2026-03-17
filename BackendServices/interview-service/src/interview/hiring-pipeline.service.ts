import { Injectable, Logger, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { HiringPipeline } from './entities/hiring-pipeline.entity';
import { UpdateStageDto } from './dto/update-stage.dto';
import { CreatePipelineDto } from './dto/create-pipeline.dto';
import { HiringStage } from '../constants';

@Injectable()
export class HiringPipelineService {
  private readonly logger = new Logger(HiringPipelineService.name);

  constructor(
    @InjectRepository(HiringPipeline)
    private readonly pipelineRepository: Repository<HiringPipeline>,
  ) {}

  async createPipeline(dto: CreatePipelineDto): Promise<HiringPipeline> {
    const existing = await this.pipelineRepository.findOne({
      where: { applicationId: dto.applicationId },
    });
    if (existing) return existing;

    const pipeline = this.pipelineRepository.create({
      ...dto,
      currentStage: HiringStage.APPLIED,
      stageHistory: [
        { stage: HiringStage.APPLIED, notes: 'Pipeline created', updatedAt: new Date().toISOString() },
      ],
    });
    const saved = await this.pipelineRepository.save(pipeline);
    this.logger.log(`Pipeline created for application ${dto.applicationId}`);
    return saved;
  }

  async advanceStage(id: string, dto: UpdateStageDto): Promise<HiringPipeline> {
    const pipeline = await this.findOneOrFail(id);
    pipeline.currentStage = dto.stage;
    pipeline.stageHistory = [
      ...pipeline.stageHistory,
      { stage: dto.stage, notes: dto.notes ?? '', updatedAt: new Date().toISOString() },
    ];
    return this.pipelineRepository.save(pipeline);
  }

  async getByEmployer(employerId: string): Promise<HiringPipeline[]> {
    return this.pipelineRepository.find({ where: { employerId } });
  }

  async getByCandidate(candidateId: string): Promise<HiringPipeline[]> {
    return this.pipelineRepository.find({ where: { candidateId } });
  }

  private async findOneOrFail(id: string): Promise<HiringPipeline> {
    const pipeline = await this.pipelineRepository.findOne({ where: { id } });
    if (!pipeline) throw new NotFoundException(`Pipeline ${id} not found`);
    return pipeline;
  }
}
