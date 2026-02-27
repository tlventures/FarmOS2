# AgriEdge Link - AWS Infrastructure Deployment Guide

This guide walks you through deploying the complete AWS infrastructure for AgriEdge Link using Terraform.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Initial Setup](#initial-setup)
3. [Deployment Steps](#deployment-steps)
4. [Post-Deployment Configuration](#post-deployment-configuration)
5. [Verification](#verification)
6. [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Tools

- **AWS Account** with administrator access
- **Terraform** >= 1.0 ([Install](https://www.terraform.io/downloads))
- **AWS CLI** v2 ([Install](https://aws.amazon.com/cli/))
- **Python** 3.11+ (for Lambda functions)
- **Make** (optional, for convenience commands)

### AWS Permissions Required

Your AWS user/role needs permissions for:
- Lambda (create, update, delete functions)
- API Gateway (create, configure REST APIs)
- DynamoDB (create, manage tables)
- S3 (create, manage buckets)
- IAM (create roles and policies)
- CloudWatch (create log groups)
- Secrets Manager (create secrets)
- Bedrock (invoke models)
- Transcribe/Polly (voice services)
- SNS (send SMS)

## Initial Setup

### 1. Configure AWS Credentials

```bash
aws configure
# Enter your AWS Access Key ID
# Enter your AWS Secret Access Key
# Default region: ap-south-1 (Mumbai)
# Default output format: json
```

Verify configuration:
```bash
aws sts get-caller-identity
```

### 2. Create Terraform State Bucket

The Terraform state is stored in S3 for team collaboration and state locking.

```bash
# Create S3 bucket for Terraform state
aws s3 mb s3://agriedge-terraform-state --region ap-south-1

# Enable versioning
aws s3api put-bucket-versioning \
  --bucket agriedge-terraform-state \
  --versioning-configuration Status=Enabled

# Enable encryption
aws s3api put-bucket-encryption \
  --bucket agriedge-terraform-state \
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "AES256"
      }
    }]
  }'

# Block public access
aws s3api put-public-access-block \
  --bucket agriedge-terraform-state \
  --public-access-block-configuration \
    "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"
```

### 3. Configure Terraform Variables

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars`:
```hcl
aws_region  = "ap-south-1"
environment = "dev"  # or "staging", "production"
project_name = "agriedge-link"

# Adjust these based on your needs
lambda_memory_size = 512
lambda_timeout     = 30
cloudwatch_log_retention_days = 30
```

## Deployment Steps

### Option 1: Using Make (Recommended)

```bash
# Initialize Terraform
make init

# Preview changes
make plan

# Deploy infrastructure
make apply
```

### Option 2: Using Deployment Script

```bash
# Preview changes
./scripts/deploy.sh plan

# Deploy infrastructure
./scripts/deploy.sh apply
```

### Option 3: Manual Terraform Commands

```bash
# Create placeholder Lambda package
./scripts/create_placeholder_lambda.sh

# Initialize Terraform
terraform init

# Preview changes
terraform plan

# Deploy infrastructure
terraform apply
```

### Deployment Time

Initial deployment takes approximately **5-10 minutes** and creates:
- 8 Lambda functions
- 1 API Gateway REST API
- 9 DynamoDB tables
- 4 S3 buckets
- IAM roles and policies
- CloudWatch log groups

## Post-Deployment Configuration

After Terraform completes, perform these manual configuration steps:

### 1. Store Secrets in AWS Secrets Manager

```bash
# JWT Secret for authentication
aws secretsmanager create-secret \
  --name agriedge-link/jwt-secret \
  --description "JWT secret for token signing" \
  --secret-string '{"secret":"REPLACE_WITH_RANDOM_256_BIT_KEY"}' \
  --region ap-south-1

# Beckn Private Key (for ONDC integration)
aws secretsmanager create-secret \
  --name agriedge-link/beckn-private-key \
  --description "Beckn protocol signing key" \
  --secret-string '{"key":"REPLACE_WITH_BECKN_PRIVATE_KEY"}' \
  --region ap-south-1

# Bhashini API Key (fallback voice service)
aws secretsmanager create-secret \
  --name agriedge-link/bhashini-api-key \
  --description "Bhashini API key for voice services" \
  --secret-string '{"api_key":"REPLACE_WITH_BHASHINI_KEY"}' \
  --region ap-south-1
```

Generate a secure JWT secret:
```bash
python3 -c "import secrets; print(secrets.token_urlsafe(32))"
```

### 2. Enable Amazon Bedrock Models

1. Open AWS Console → Amazon Bedrock
2. Navigate to "Model access"
3. Request access to:
   - **Anthropic Claude 3 Sonnet** (for treatment recommendations)
   - **Amazon Titan Embed Text** (for knowledge base embeddings)
4. Wait for approval (usually instant for most regions)

### 3. Configure SNS for SMS (OTP Delivery)

```bash
# Set SMS preferences
aws sns set-sms-attributes \
  --attributes DefaultSMSType=Transactional \
  --region ap-south-1

# Request production SMS access (if needed)
# Go to AWS Console → SNS → Text messaging (SMS) → Sandbox
# Request production access for sending SMS to any number
```

### 4. Deploy Lambda Function Code

The Lambda functions are created with placeholder code. Deploy actual implementations:

```bash
# Example: Deploy auth Lambda
cd ../lambda/auth
pip install -r requirements.txt -t .
zip -r auth.zip .

aws lambda update-function-code \
  --function-name agriedge-link-auth-dev \
  --zip-file fileb://auth.zip \
  --region ap-south-1
```

Repeat for all Lambda functions:
- `agriedge-link-auth-dev`
- `agriedge-link-user-profile-dev`
- `agriedge-link-sync-dev`
- `agriedge-link-market-connector-dev`
- `agriedge-link-telemetry-dev`
- `agriedge-link-treatment-generator-dev`
- `agriedge-link-voice-interface-dev`
- `agriedge-link-presigned-url-dev`

### 5. Upload ML Models to S3

```bash
# Upload TFLite model
aws s3 cp crop_disease_classifier.tflite \
  s3://agriedge-link-models-dev/production/crop_disease_classifier_v1.0.0.tflite

# Upload model metadata
aws s3 cp model_metadata.json \
  s3://agriedge-link-models-dev/production/latest.json
```

## Verification

### 1. Check Infrastructure Status

```bash
# View all outputs
terraform output

# Get API Gateway URL
terraform output api_gateway_url
```

### 2. Test API Endpoints

```bash
# Get API URL
API_URL=$(terraform output -raw api_gateway_url)

# Test auth registration (should return 400 without body)
curl -X POST $API_URL/api/v1/auth/register \
  -H "Content-Type: application/json"

# Expected: {"message": "Missing required fields"}
```

### 3. Check Lambda Functions

```bash
# List all Lambda functions
aws lambda list-functions --region ap-south-1 | grep agriedge-link

# Test a Lambda function
aws lambda invoke \
  --function-name agriedge-link-auth-dev \
  --payload '{"test": true}' \
  --region ap-south-1 \
  response.json

cat response.json
```

### 4. Check DynamoDB Tables

```bash
# List all tables
aws dynamodb list-tables --region ap-south-1 | grep agriedge-link

# Describe a table
aws dynamodb describe-table \
  --table-name agriedge-link-users-dev \
  --region ap-south-1
```

### 5. Check CloudWatch Logs

```bash
# View Lambda logs
aws logs tail /aws/lambda/agriedge-link-auth-dev --follow

# View API Gateway logs
aws logs tail /aws/apigateway/agriedge-link-dev --follow
```

## Monitoring and Maintenance

### CloudWatch Dashboards

Create a custom dashboard to monitor:
- API Gateway request count and latency
- Lambda invocations and errors
- DynamoDB read/write capacity
- S3 bucket size

### Alarms

Set up CloudWatch alarms for:
- Lambda error rate > 5%
- API Gateway 5xx errors > 10
- DynamoDB throttling events
- High Lambda duration (> 25 seconds)

### Cost Monitoring

Enable AWS Cost Explorer and set up:
- Budget alerts ($1000/month threshold)
- Cost anomaly detection
- Service-level cost tracking

## Troubleshooting

### Issue: Terraform State Lock

**Error**: "Error acquiring the state lock"

**Solution**:
```bash
# Force unlock (use with caution)
terraform force-unlock LOCK_ID
```

### Issue: Lambda Function Timeout

**Error**: "Task timed out after 30.00 seconds"

**Solution**: Increase timeout in `variables.tf`:
```hcl
variable "lambda_timeout" {
  default = 60  # Increase to 60 seconds
}
```

### Issue: DynamoDB Throttling

**Error**: "ProvisionedThroughputExceededException"

**Solution**: Switch to provisioned capacity or increase on-demand limits.

### Issue: Bedrock Access Denied

**Error**: "AccessDeniedException: Could not access model"

**Solution**: Enable model access in Bedrock console (see step 2 above).

### Issue: API Gateway 403 Forbidden

**Error**: "Missing Authentication Token"

**Solution**: Check API Gateway deployment and stage configuration.

## Cleanup

To destroy all infrastructure:

```bash
# Using Make
make destroy

# Using script
./scripts/deploy.sh destroy

# Using Terraform
terraform destroy
```

**Warning**: This will permanently delete all data in DynamoDB tables and S3 buckets!

## Next Steps

After successful deployment:

1. ✅ Deploy Lambda function code
2. ✅ Configure secrets in Secrets Manager
3. ✅ Enable Bedrock models
4. ✅ Test API endpoints
5. ✅ Set up monitoring and alarms
6. ✅ Configure custom domain (optional)
7. ✅ Set up CI/CD pipeline
8. ✅ Deploy to staging/production environments

## Support

For issues or questions:
- Check CloudWatch Logs for errors
- Review AWS service quotas
- Consult [AWS Documentation](https://docs.aws.amazon.com/)
- Review [Terraform AWS Provider Docs](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
