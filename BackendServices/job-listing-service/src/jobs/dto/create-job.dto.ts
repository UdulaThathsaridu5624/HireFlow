import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsString,
  IsNotEmpty,
  IsArray,
  IsOptional,
  IsNumber,
  IsBoolean,
  IsDateString,
  Min,
} from 'class-validator';

export class CreateJobDto {
  @ApiProperty({ example: 'Senior Backend Engineer' })
  @IsString()
  @IsNotEmpty()
  title: string;

  @ApiProperty({ example: 'We are looking for a skilled backend engineer...' })
  @IsString()
  @IsNotEmpty()
  description: string;

  @ApiProperty({ example: ['Node.js', 'PostgreSQL', 'Docker'] })
  @IsArray()
  @IsString({ each: true })
  requiredSkills: string[];

  @ApiPropertyOptional({ example: 80000 })
  @IsOptional()
  @IsNumber()
  @Min(0)
  salaryMin?: number;

  @ApiPropertyOptional({ example: 120000 })
  @IsOptional()
  @IsNumber()
  @Min(0)
  salaryMax?: number;

  @ApiPropertyOptional({ example: 'USD' })
  @IsOptional()
  @IsString()
  salaryCurrency?: string;

  @ApiProperty({ example: 'Colombo, Sri Lanka' })
  @IsString()
  @IsNotEmpty()
  location: string;

  @ApiPropertyOptional({ example: true })
  @IsOptional()
  @IsBoolean()
  isRemote?: boolean;

  @ApiProperty({ example: '2026-06-30T23:59:59.000Z' })
  @IsDateString()
  deadline: string;
}
