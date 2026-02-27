# AgriEdge Link - Terraform Infrastructure

This directory contains Terraform configuration for deploying the AgriEdge Link AWS infrastructure.

## Architecture Overview

The infrastructure includes:

- **Lambda Functions**: 8 serverless functions for various services
  - Auth (registration, OTP verification)
  - User Profile management
  - Data Sync
  - Market Connector (Beckn/ONDC integration)
  - Telemetry
  - Treatment Generator (Amazon Bedrock)
  - Voice Interface (Transcribe/Polly)
  - Presigned URL generator

- **API Gateway**: REST API with regional endpoints

- **DynamoDB Tables**: 9 NoSQL tables for data storage
  - Users
  - User Profiles
  - Diagnoses
  - Transactions
  - Provider Ratings
  - Telemetry Events
  - Treatment Recommendations (cache)
  - Beckn Search Cache
  - Sync Queue

- **S3 Buckets**: 4 buckets for object storage
  - Images (diagnostic photos)
  - Models (TFLite ML models)
  - Audio (voice recordings)
  - Lambda Deployments

- **IAM Roles & Policies**: Least-privilege access for Lambda functions

- **CloudWatch**: Logging and monitoring for all services

## Prerequisites

1. **AWS Account** with appropriate permissions
2. **Terraform** >= 1.0 installed
3. **AWS CLI** configured with credentials
4. **S3 Backend Bucket** for Terraform state (create manually first)

## Initial Setup

### 1. Create Terraform State Bucket

```bash
aws s3 mb s3://agriedge-terraform-state --region ap-south-1
aws s3api put-bucket-versioning \
  --bucket agriedge-terraform-state \
  --versioning-configuration Status=Enabled
```

### 2. Create Lambda Placeholder Package

Before running Terraform, create a placeholder Lambda deployment package:

```bash
cd terraform
echo 'def lambda_handler(event, context): return {"statusCode": 200}' > lambda_placeholder.py
zip lambda_placeholder.zip lambda_placeholder.py
```

### 3. Configure Variables

Copy the example variables file and update with your values:

```bash
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your configuration
```

## Deployment

### Initialize Terraform

```bash
terraform init
```

### Plan Infrastructure Changes

```bash
terraform plan
```

### Apply Infrastructure

```bash
terraform apply
```

### Destroy Infrastructure (when needed)

```bash
terraform destroy
```

## Post-Deployment Steps

After Terraform creates the infrastructure:

### 1. Deploy Lambda Functions

The Lambda functions are created with placeholder code. You need to deploy actual function code:

```bash
# Example for auth Lambda
cd ../lambda/auth
pip install -r requirements.txt -t .
zip -r auth.zip .
aws lambda update-function-code \
  --function-name agriedge-link-auth-dev \
  --zip-file fileb://auth.zip
```

### 2. Configure Secrets Manager

Store sensitive configuration in AWS Secrets Manager:

```bash
# JWT Secret
aws secretsmanager create-secret \
  --name agriedge-link/jwt-secret \
  --secret-string '{"secret":"your-jwt-secret-key"}' \
  --region ap-south-1

# Beckn Private Key
aws secretsmanager create-secret \
  --name agriedge-link/beckn-private-key \
  --secret-string '{"key":"your-beckn-private-key"}' \
  --region ap-south-1
```

### 3. Enable Amazon Bedrock Models

Enable access to Bedrock models in the AWS Console:
- Navigate to Amazon Bedrock
- Request access to Claude 3 Sonnet
- Request access to Titan Embed Text

### 4. Configure API Gateway Custom Domain (Optional)

Set up a custom domain for your API:

```bash
# Create certificate in ACM
# Configure custom domain in API Gateway
# Update DNS records
```

### 5. Test API Endpoints

```bash
# Get API Gateway URL
API_URL=$(terraform output -raw api_gateway_url)

# Test health endpoint
curl $API_URL/api/v1/health
```

## Monitoring

### CloudWatch Logs

View logs for each Lambda function:

```bash
aws logs tail /aws/lambda/agriedge-link-auth-dev --follow
```

### CloudWatch Metrics

Monitor API Gateway and Lambda metrics in the AWS Console:
- API Gateway → Stages → Metrics
- Lambda → Functions → Monitoring

### X-Ray Tracing

View distributed traces in AWS X-Ray console for request flow analysis.

## Cost Optimization

Current configuration uses:
- **DynamoDB**: On-demand billing (pay per request)
- **Lambda**: Pay per invocation
- **S3**: Lifecycle policies to move old data to Glacier
- **CloudWatch**: 30-day log retention

Estimated monthly cost for 10,000 active users: ~$850/month

## Security Considerations

1. **Encryption**: All data encrypted at rest (DynamoDB, S3) and in transit (TLS 1.3)
2. **IAM**: Least-privilege policies for Lambda functions
3. **API Gateway**: Throttling enabled (5000 burst, 2000 rate)
4. **Secrets**: Sensitive data stored in Secrets Manager
5. **Logging**: All API calls logged to CloudWatch

## Troubleshooting

### Lambda Function Errors

Check CloudWatch Logs:
```bash
aws logs tail /aws/lambda/FUNCTION_NAME --follow
```

### API Gateway 5xx Errors

Check API Gateway execution logs in CloudWatch.

### DynamoDB Throttling

If you see throttling errors, consider switching to provisioned capacity or increasing on-demand limits.

## Infrastructure Updates

To update infrastructure:

1. Modify Terraform files
2. Run `terraform plan` to preview changes
3. Run `terraform apply` to apply changes

## Backup and Disaster Recovery

- **DynamoDB**: Point-in-time recovery enabled
- **S3**: Versioning enabled
- **Terraform State**: Stored in S3 with versioning

## Support

For issues or questions:
- Check CloudWatch Logs
- Review AWS service quotas
- Consult AWS documentation
