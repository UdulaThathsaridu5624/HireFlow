import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsEnum, IsOptional, IsString } from 'class-validator';
import { HiringStage } from '../../constants';

export class UpdateStageDto {
  @ApiProperty({ enum: HiringStage })
  @IsEnum(HiringStage)
  stage: HiringStage;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  notes?: string;
}
