import {
  Entity,
  PrimaryGeneratedColumn,
  Column,
  CreateDateColumn,
  UpdateDateColumn,
} from 'typeorm';
import { JobStatus } from '../../constants';

@Entity('jobs')
export class Job {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column()
  employerId: string; // ID of the employer who posted the job (from JWT)

  @Column({ nullable: true })
  companyId: string; // ID from Company Service (Tharindu)

  @Column()
  title: string;

  @Column('text')
  description: string;

  @Column('simple-array')
  requiredSkills: string[]; // e.g. ["Node.js", "PostgreSQL", "Docker"]

  @Column({ nullable: true })
  salaryMin: number;

  @Column({ nullable: true })
  salaryMax: number;

  @Column({ nullable: true })
  salaryCurrency: string; // e.g. "USD", "LKR"

  @Column()
  location: string;

  @Column({ nullable: true })
  isRemote: boolean;

  @Column({ type: 'timestamp' })
  deadline: Date;

  @Column({
    type: 'enum',
    enum: JobStatus,
    default: JobStatus.OPEN,
  })
  status: JobStatus;

  @CreateDateColumn()
  createdAt: Date;

  @UpdateDateColumn()
  updatedAt: Date;
}
