import { ApiProperty } from '@nestjs/swagger';
import { IsNotEmpty, IsString } from 'class-validator';

export class CreatePipelineDto {
  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  applicationId: string;

  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  jobId: string;

  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  candidateId: string;

  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  employerId: string;
}
