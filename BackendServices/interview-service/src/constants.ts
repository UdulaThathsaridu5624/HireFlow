export enum ServiceTokens {
  AUTH_SERVICE = 'AUTH_SERVICE',
}

export enum Queues {
  AUTH_VALIDATE = 'auth_validate_queue',
  HIREFLOW_EVENTS = 'hireflow_events_queue',
}

export enum AuthEvents {
  USER_REGISTERED = 'user.registered',
  USER_LOGGED_IN = 'user.loggedIn',
  AUTH_VALIDATE = 'auth.validate',
}
