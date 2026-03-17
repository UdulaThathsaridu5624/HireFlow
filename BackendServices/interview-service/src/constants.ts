export enum ServiceTokens {
  AUTH_SERVICE = 'AUTH_SERVICE',
  INTERVIEW_PUBLISHER = 'INTERVIEW_PUBLISHER',
}

export enum Queues {
  AUTH_VALIDATE = 'auth_validate_queue',
  HIREFLOW_EVENTS = 'hireflow_events_queue',
  INTERVIEW_EVENTS = 'interview_events_queue',
}

export enum AuthEvents {
  USER_REGISTERED = 'user.registered',
  USER_LOGGED_IN = 'user.loggedIn',
  AUTH_VALIDATE = 'auth.validate',
}

export enum InterviewEvents {
  INTERVIEW_SCHEDULED = 'interview.scheduled',
  INTERVIEW_UPDATED = 'interview.updated',
  INTERVIEW_CANCELLED = 'interview.cancelled',
  APPLICATION_FORWARDED = 'application.forwarded',
}

export enum HiringStage {
  APPLIED = 'APPLIED',
  SCREENING = 'SCREENING',
  INTERVIEW = 'INTERVIEW',
  OFFER = 'OFFER',
  HIRED = 'HIRED',
  REJECTED = 'REJECTED',
}

export enum InterviewFormat {
  ONLINE = 'ONLINE',
  IN_PERSON = 'IN_PERSON',
}

export enum InterviewStatus {
  SCHEDULED = 'SCHEDULED',
  ACCEPTED = 'ACCEPTED',
  DECLINED = 'DECLINED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
}
