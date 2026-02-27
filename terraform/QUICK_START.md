# AgriEdge Link - Quick Start Guide

Get the infrastructure up and running in 5 minutes!

## Prerequisites

- AWS Account with credentials configured
- Terraform installed
- AWS CLI installed

## Quick Deploy

### 1. Create State Bucket (One-time setup)

```bash
aws s3 mb s3://agriedge-terraform-state --region ap-south-1
aws s3api put-bucket-versioning \
  --bucket agriedge-terraform-state \
  --versioning-configuration Status=Enabled
```

### 2. Configure Variables

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars if needed (defaults are fine for dev)
```

### 3. Deploy

```bash
# Using Make (recommended)
make init
make apply

# OR using script
./scripts/deploy.sh apply

# OR using Terraform directly
./scripts/create_placeholder_lambda.sh
terraform init
terraform apply
```

### 4. Get API URL

```bash
terraform output api_gateway_url
```

## Post-Deployment (Required)

### 1. Create JWT Secret

```bash
# Generate secret
JWT_SECRET=$(python3 -c "import secrets; print(secrets.token_urlsafe(32))")

# Store in Secrets Manager
aws secretsmanager create-secret \
  --name agriedge-link/jwt-secret \
  --secret-string "{\"secret\":\"$JWT_SECRET\"}" \
  --region ap-south-1
```

### 2. Enable Bedrock Models

1. Go to AWS Console → Amazon Bedrock
2. Click "Model access"
3. Enable "Claude 3 Sonnet" and "Titan Embed Text"

### 3. Deploy Lambda Code

```bash
# After implementing Lambda functions, deploy them:
aws lambda update-function-code \
  --function-name agriedge-link-auth-dev \
  --zip-file fileb://auth.zip \
  --region ap-south-1
```

## Verify Deployment

```bash
# Check all resources
terraform state list

# Test API
API_URL=$(terraform output -raw api_gateway_url)
curl $API_URL/api/v1/health
```

## Common Commands

```bash
# View outputs
terraform output

# View logs
aws logs tail /aws/lambda/agriedge-link-auth-dev --follow

# Update infrastructure
terraform apply

# Destroy everything
terraform destroy
```

## Troubleshooting

**Issue**: "Error acquiring state lock"
```bash
terraform force-unlock LOCK_ID
```

**Issue**: Lambda placeholder error
```bash
./scripts/create_placeholder_lambda.sh
terraform apply
```

**Issue**: AWS credentials not found
```bash
aws configure
```

## Next Steps

1. ✅ Deploy Lambda function code
2. ✅ Configure Secrets Manager
3. ✅ Enable Bedrock models
4. ✅ Test API endpoints
5. ✅ Set up monitoring

## Documentation

- Full deployment guide: `DEPLOYMENT_GUIDE.md`
- Infrastructure details: `INFRASTRUCTURE_SUMMARY.md`
- Terraform docs: `README.md`

## Support

For detailed instructions, see `DEPLOYMENT_GUIDE.md`
