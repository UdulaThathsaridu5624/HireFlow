import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
  ManyToOne,
  JoinColumn,
  Unique,
} from 'typeorm';
import { Job } from '../../jobs/entities/job.entity';
import { ApplicationStatus } from '../../constants';

@Entity('applications')
@Unique(['candidateId', 'jobId']) // A candidate can only apply once per job
export class Application {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column()
  candidateId: string; // From JWT payload

  @Column()
  jobId: string;

  @ManyToOne(() => Job, { eager: true, onDelete: 'CASCADE' })
  @JoinColumn({ name: 'jobId' })
  job: Job;

  // CV / application form fields
  @Column('text')
  coverLetter: string;

  @Column('simple-array')
  skills: string[];

  @Column('text')
  experience: string; // Work experience summary

  @Column('text')
  education: string; // Education summary

  @Column({ nullable: true })
  resumeUrl: string; // Optional link to resume

  @Column({ nullable: true })
  portfolioUrl: string;

  @Column({
    type: 'enum',
    enum: ApplicationStatus,
    default: ApplicationStatus.PENDING,
  })
  status: ApplicationStatus;

  @CreateDateColumn()
  appliedAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
