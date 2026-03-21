# HireFlow - Job Listing Service

Microservice for managing job postings and candidate applications.
Built with **NestJS + TypeScript + PostgreSQL**.

## Endpoints Summary

| Method | Route | Role | Description |
|--------|-------|------|-------------|
| POST | /jobs | Employer | Post a new job |
| PATCH | /jobs/:id | Employer | Edit a job |
| PATCH | /jobs/:id/close | Employer | Close a job |
| GET | /jobs/my-jobs | Employer | View own job listings |
| GET | /jobs | Public | Browse all open jobs |
| GET | /jobs/search | Public | Search & filter jobs |
| GET | /jobs/:id | Public | View job details with company info |
| GET | /jobs/:id/verify | Internal | Used by Application Service |
| POST | /applications/jobs/:jobId/apply | Candidate | Apply to a job |
| GET | /applications/my-applications | Candidate | View own applications |
| PATCH | /applications/:id/withdraw | Candidate | Withdraw application |
| GET | /applications/jobs/:jobId | Employer | View applications for a job |
| GET | /applications/:id | Employer | View a candidate's CV |

## Running Locally

```bash
# 1. Install dependencies
npm install

# 2. Set up environment
cp .env.example .env

# 3. Start with Docker Compose (includes PostgreSQL)
docker-compose up

# OR run directly
npm run start:dev
```

## Swagger UI
Visit `http://localhost:3002/api/docs` after starting the service.

## Inter-Service Communication

| Direction | Target | How |
|-----------|--------|-----|
| → Company Service (Tharindu) | Fetch company profile per employer | HTTP GET |
| ← Application Service (Madhini) | Verify job is open | HTTP GET /jobs/:id/verify |

## CI/CD Pipeline
GitHub Actions → Lint → Test → SonarCloud → Docker Build → AWS ECS Deploy

## Required GitHub Secrets

| Secret | Description |
|--------|-------------|
| DOCKERHUB_USERNAME | Docker Hub username |
| DOCKERHUB_TOKEN | Docker Hub access token |
| SONAR_TOKEN | SonarCloud token |
| AWS_ACCESS_KEY_ID | AWS IAM key |
| AWS_SECRET_ACCESS_KEY | AWS IAM secret |
| AWS_REGION | e.g. ap-south-1 |
