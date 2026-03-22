import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsString, IsNotEmpty, IsArray, IsOptional, IsUrl } from 'class-validator';

export class ApplyToJobDto {
  @ApiProperty({ example: 'I am passionate about backend development and...' })
  @IsString()
  @IsNotEmpty()
  coverLetter: string;

  @ApiProperty({ example: ['Node.js', 'PostgreSQL', 'REST APIs'] })
  @IsArray()
  @IsString({ each: true })
  skills: string[];

  @ApiProperty({ example: '3 years at XYZ Corp as a backend developer...' })
  @IsString()
  @IsNotEmpty()
  experience: string;

  @ApiProperty({ example: 'BSc in Computer Science, University of Colombo...' })
  @IsString()
  @IsNotEmpty()
  education: string;

  @ApiPropertyOptional({ example: 'https://drive.google.com/myresume.pdf' })
  @IsOptional()
  @IsString()
  resumeUrl?: string;

  @ApiPropertyOptional({ example: 'https://github.com/jameela' })
  @IsOptional()
  @IsString()
  portfolioUrl?: string;
}
