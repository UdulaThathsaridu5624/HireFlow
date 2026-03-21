import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsDateString,
  IsEnum,
  IsInt,
  IsNotEmpty,
  IsOptional,
  IsString,
  IsUrl,
  Min,
} from 'class-validator';
import { InterviewFormat } from '../../constants';

export class CreateInterviewDto {
  @ApiProperty({ description: 'The applicationId from the hiring pipeline' })
  @IsString()
  @IsNotEmpty()
  applicationId: string;

  @ApiProperty()
  @IsDateString()
  scheduledAt: string;

  @ApiProperty({ minimum: 15 })
  @IsInt()
  @Min(15)
  durationMinutes: number;

  @ApiProperty({ enum: InterviewFormat })
  @IsEnum(InterviewFormat)
  format: InterviewFormat;

  @ApiPropertyOptional()
  @IsOptional()
  @IsUrl()
  meetingLink?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  notes?: string;
}
