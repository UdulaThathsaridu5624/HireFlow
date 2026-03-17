import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsDateString, IsEnum, IsInt, IsOptional, IsString, IsUrl, Min } from 'class-validator';
import { InterviewFormat, InterviewStatus } from '../../constants';

export class UpdateInterviewDto {
  @ApiPropertyOptional()
  @IsOptional()
  @IsDateString()
  scheduledAt?: string;

  @ApiPropertyOptional({ minimum: 15 })
  @IsOptional()
  @IsInt()
  @Min(15)
  durationMinutes?: number;

  @ApiPropertyOptional({ enum: InterviewFormat })
  @IsOptional()
  @IsEnum(InterviewFormat)
  format?: InterviewFormat;

  @ApiPropertyOptional()
  @IsOptional()
  @IsUrl()
  meetingLink?: string;

  @ApiPropertyOptional({ enum: InterviewStatus })
  @IsOptional()
  @IsEnum(InterviewStatus)
  status?: InterviewStatus;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  notes?: string;
}
