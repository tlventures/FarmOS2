# AgriEdge Link - Infrastructure Summary

## Overview

This document provides a high-level overview of the AWS infrastructure deployed for AgriEdge Link.

## Architecture Components

### Compute Layer

#### Lambda Functions (8 total)

1. **Auth Lambda** (`agriedge-link-auth-dev`)
   - Handles user registration and OTP verification
   - Generates JWT tokens
   - Sends SMS via Amazon SNS
   - Memory: 512 MB, Timeout: 30s

2. **User Profile Lambda** (`agriedge-link-user-profile-dev`)
   - Manages user profile CRUD operations
   - Stores location and crop preferences
   - Memory: 512 MB, Timeout: 30s

3. **Sync Lambda** (`agriedge-link-sync-dev`)
   - Processes batch data synchronization from mobile devices
   - Handles diagnoses, ratings, and telemetry
   - Memory: 1024 MB, Timeout: 60s

4. **Market Connector Lambda** (`agriedge-link-market-connector-dev`)
   - Integrates with Beckn/ONDC protocol
   - Searches for buyers, cold storage, equipment
   - Manages transactions
   - Memory: 512 MB, Timeout: 30s

5. **Telemetry Lambda** (`agriedge-link-telemetry-dev`)
   - Collects and anonymizes usage data
   - Stores events with 90-day TTL
   - Memory: 512 MB, Timeout: 30s

6. **Treatment Generator Lambda** (`agriedge-link-treatment-generator-dev`)
   - Uses Amazon Bedrock (Claude 3) for AI-generated recommendations
   - Caches results in DynamoDB
   - Memory: 512 MB, Timeout: 60s

7. **Voice Interface Lambda** (`agriedge-link-voice-interface-dev`)
   - Integrates with Amazon Transcribe (speech-to-text)
   - Integrates with Amazon Polly (text-to-speech)
   - Supports 6 Indian languages
   - Memory: 512 MB, Timeout: 60s

8. **Presigned URL Lambda** (`agriedge-link-presigned-url-dev`)
   - Generates presigned S3 URLs for direct image uploads
   - 15-minute expiration
   - Memory: 256 MB, Timeout: 30s

### API Layer

#### API Gateway REST API

- **Name**: `agriedge-link-api-dev`
- **Type**: Regional endpoint
- **Stage**: `dev`
- **Throttling**: 5000 burst, 2000 rate limit
- **Features**:
  - CORS enabled
  - CloudWatch logging
  - X-Ray tracing
  - Request validation

**API Endpoints**:
```
POST   /api/v1/auth/register
POST   /api/v1/auth/verify-otp
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout
GET    /api/v1/profile
PUT    /api/v1/profile
DELETE /api/v1/profile
POST   /api/v1/sync/diagnoses
POST   /api/v1/sync/ratings
GET    /api/v1/sync/status
POST   /api/v1/market/search/buyers
POST   /api/v1/market/search/cold-storage
POST   /api/v1/market/search/equipment
POST   /api/v1/market/transactions
GET    /api/v1/market/transactions/:id
GET    /api/v1/market/providers/:id/reviews
POST   /api/v1/telemetry/events
GET    /api/v1/treatments/:diseaseId
POST   /api/v1/voice/speech-to-text
POST   /api/v1/voice/text-to-speech
GET    /api/v1/presigned-url
```

### Data Layer

#### DynamoDB Tables (9 total)

1. **Users Table** (`agriedge-link-users-dev`)
   - Partition Key: `user_id`
   - GSI: `PhoneNumberIndex` (phone_number)
   - Point-in-time recovery enabled
   - Encryption at rest

2. **User Profiles Table** (`agriedge-link-user-profiles-dev`)
   - Partition Key: `user_id`
   - GSI: `LocationIndex` (state, district)
   - Stores location and crop preferences

3. **Diagnoses Table** (`agriedge-link-diagnoses-dev`)
   - Partition Key: `user_id`, Sort Key: `timestamp`
   - GSI: `DiagnosisIdIndex`, `DiseaseIndex`
   - Stores crop disease diagnoses

4. **Transactions Table** (`agriedge-link-transactions-dev`)
   - Partition Key: `user_id`, Sort Key: `created_at`
   - GSI: `TransactionIdIndex`, `StatusIndex`
   - Stores market transactions

5. **Provider Ratings Table** (`agriedge-link-provider-ratings-dev`)
   - Partition Key: `provider_id`, Sort Key: `created_at`
   - GSI: `UserRatingsIndex`
   - Stores farmer reviews

6. **Telemetry Events Table** (`agriedge-link-telemetry-events-dev`)
   - Partition Key: `event_type`, Sort Key: `timestamp`
   - TTL enabled (90 days)
   - Anonymized usage data

7. **Treatment Recommendations Table** (`agriedge-link-treatment-recommendations-dev`)
   - Partition Key: `disease_id`, Sort Key: `language`
   - TTL enabled (30 days)
   - Caches Bedrock-generated recommendations

8. **Beckn Search Cache Table** (`agriedge-link-beckn-search-cache-dev`)
   - Partition Key: `transaction_id`
   - TTL enabled (1 hour)
   - Caches market search results

9. **Sync Queue Table** (`agriedge-link-sync-queue-dev`)
   - Partition Key: `user_id`, Sort Key: `timestamp`
   - GSI: `StatusIndex`
   - Tracks pending synchronization items

**Billing Mode**: Pay-per-request (on-demand)

#### S3 Buckets (4 total)

1. **Images Bucket** (`agriedge-link-images-dev`)
   - Stores diagnostic images
   - Versioning enabled
   - Lifecycle: Move to Glacier after 90 days
   - Encryption: AES-256
   - Structure: `diagnoses/{user_id}/{year}/{month}/{diagnosis_id}.jpg`

2. **Models Bucket** (`agriedge-link-models-dev`)
   - Stores TFLite ML models
   - Versioning enabled
   - Encryption: AES-256
   - Structure: `production/`, `staging/`

3. **Audio Bucket** (`agriedge-link-audio-dev`)
   - Temporary storage for voice recordings
   - Lifecycle: Delete after 7 days
   - Encryption: AES-256

4. **Lambda Deployments Bucket** (`agriedge-link-lambda-deployments-dev`)
   - Stores Lambda deployment packages
   - Versioning enabled
   - Encryption: AES-256

### Security Layer

#### IAM Roles

1. **Lambda Execution Role** (`agriedge-link-lambda-execution-dev`)
   - Attached policies:
     - AWSLambdaBasicExecutionRole (CloudWatch Logs)
     - Custom DynamoDB access policy
     - Custom S3 access policy
     - Custom Bedrock access policy
     - Custom Transcribe/Polly access policy
     - Custom SNS access policy
     - Custom Secrets Manager access policy

2. **API Gateway CloudWatch Role** (`agriedge-link-api-gateway-cloudwatch-dev`)
   - Allows API Gateway to write logs to CloudWatch

#### Security Features

- **Encryption at Rest**: All DynamoDB tables and S3 buckets
- **Encryption in Transit**: TLS 1.3 for all API calls
- **Least Privilege**: IAM policies grant minimum required permissions
- **Secrets Management**: Sensitive data stored in AWS Secrets Manager
- **API Throttling**: Rate limiting to prevent abuse
- **CORS**: Configured for mobile app access

### Monitoring Layer

#### CloudWatch Log Groups (9 total)

- `/aws/apigateway/agriedge-link-dev` (API Gateway logs)
- `/aws/lambda/agriedge-link-auth-dev`
- `/aws/lambda/agriedge-link-user-profile-dev`
- `/aws/lambda/agriedge-link-sync-dev`
- `/aws/lambda/agriedge-link-market-connector-dev`
- `/aws/lambda/agriedge-link-telemetry-dev`
- `/aws/lambda/agriedge-link-treatment-generator-dev`
- `/aws/lambda/agriedge-link-voice-interface-dev`
- `/aws/lambda/agriedge-link-presigned-url-dev`

**Retention**: 30 days

#### Monitoring Features

- **X-Ray Tracing**: Enabled for API Gateway and Lambda
- **CloudWatch Metrics**: Automatic for all services
- **Access Logs**: JSON-formatted API Gateway logs
- **Lambda Insights**: Available for performance monitoring

## AI/ML Services Integration

### Amazon Bedrock

- **Model**: Claude 3 Sonnet
- **Use Case**: Generate treatment recommendations
- **Caching**: Results cached in DynamoDB (30-day TTL)

### Amazon Transcribe

- **Languages**: Hindi, Marathi, Tamil, Telugu, Kannada, Bengali
- **Use Case**: Speech-to-text for voice interface
- **Audio Storage**: Temporary S3 storage (7-day lifecycle)

### Amazon Polly

- **Voice**: Aditi (Indian English/Hindi)
- **Use Case**: Text-to-speech for voice output
- **Format**: MP3

### Amazon SNS

- **Use Case**: SMS delivery for OTP
- **Type**: Transactional SMS
- **Region**: ap-south-1 (Mumbai)

## Cost Estimation

### Monthly Cost Breakdown (10,000 active users)

| Service | Usage | Cost |
|---------|-------|------|
| Lambda | 5M requests, 500ms avg | $25 |
| API Gateway | 5M requests | $18 |
| DynamoDB | 50GB, 10M reads, 2M writes | $150 |
| S3 Standard | 500GB | $30 |
| S3 Glacier | 2TB | $8 |
| RDS PostgreSQL | db.t3.medium | $75 |
| Bedrock (Claude 3) | 5M input, 2M output tokens | $60 |
| Amazon Q | 100 users | $200 |
| Transcribe | 10,000 minutes | $24 |
| Polly | 5M characters | $20 |
| CloudFront | 1TB transfer | $85 |
| Data Transfer | 500GB | $45 |
| CloudWatch | 50GB logs | $15 |
| SNS SMS | 5,000 messages | $25 |
| ElastiCache | cache.t3.micro | $15 |
| **Total** | | **~$854/month** |

### Cost Optimization

- On-demand DynamoDB billing (no idle costs)
- S3 lifecycle policies (Glacier for old data)
- Lambda pay-per-use (no idle costs)
- CloudWatch log retention (30 days)
- Aggressive caching (CloudFront, ElastiCache)

## Scalability

### Current Capacity

- **API Gateway**: 10,000 requests/second (burst)
- **Lambda**: 1,000 concurrent executions (default)
- **DynamoDB**: Unlimited (on-demand)
- **S3**: Unlimited storage

### Scaling Strategy

1. **Horizontal Scaling**: Lambda auto-scales with load
2. **Database Scaling**: DynamoDB on-demand handles spikes
3. **CDN**: CloudFront reduces origin load
4. **Caching**: ElastiCache reduces database queries

### Performance Targets

- API response time: < 500ms (p95)
- Lambda cold start: < 2s
- DynamoDB query: < 10ms
- S3 upload: < 5s (1MB image)

## Disaster Recovery

### Backup Strategy

- **DynamoDB**: Point-in-time recovery (35 days)
- **S3**: Versioning enabled
- **Terraform State**: Versioned in S3
- **Lambda Code**: Versioned in S3

### Recovery Objectives

- **RTO** (Recovery Time Objective): 1 hour
- **RPO** (Recovery Point Objective): 5 minutes

### High Availability

- **Multi-AZ**: DynamoDB, S3, Lambda (automatic)
- **Regional**: API Gateway, CloudFront
- **Failover**: Automatic for managed services

## Compliance and Security

### Data Protection

- Encryption at rest (AES-256)
- Encryption in transit (TLS 1.3)
- Data anonymization (telemetry)
- PII protection (hashed user IDs)

### Access Control

- IAM roles with least privilege
- API Gateway authorization
- JWT token authentication
- Secrets Manager for credentials

### Audit and Logging

- CloudWatch Logs (all API calls)
- CloudTrail (AWS API calls)
- X-Ray tracing (request flow)
- Access logs (S3, API Gateway)

## Maintenance

### Regular Tasks

- **Weekly**: Review CloudWatch alarms
- **Monthly**: Analyze cost reports
- **Quarterly**: Update Lambda runtimes
- **Annually**: Review IAM policies

### Updates

- Lambda runtime updates (Python 3.11+)
- Terraform provider updates
- Security patches (automatic for managed services)
- ML model updates (OTA to mobile devices)

## Support and Documentation

- **Terraform Docs**: See `README.md`
- **Deployment Guide**: See `DEPLOYMENT_GUIDE.md`
- **API Documentation**: See design.md in specs
- **AWS Documentation**: [docs.aws.amazon.com](https://docs.aws.amazon.com/)
