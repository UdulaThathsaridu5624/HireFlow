import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
} from 'typeorm';
import { HiringStage } from '../../constants';

@Entity('hiring_pipelines')
export class HiringPipeline {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column()
  applicationId: string;

  @Column()
  jobId: string;

  @Column()
  candidateId: string;

  @Column()
  employerId: string;

  @Column({ type: 'enum', enum: HiringStage, default: HiringStage.APPLIED })
  currentStage: HiringStage;

  @Column({ type: 'jsonb', default: [] })
  stageHistory: { stage: HiringStage; notes: string; updatedAt: string }[];

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
