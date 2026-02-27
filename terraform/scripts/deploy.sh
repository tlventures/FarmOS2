#!/bin/bash

# Deployment script for AgriEdge Link infrastructure
# Usage: ./scripts/deploy.sh [plan|apply|destroy]

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TERRAFORM_DIR="$(dirname "$SCRIPT_DIR")"
cd "$TERRAFORM_DIR"

ACTION=${1:-plan}

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}AgriEdge Link Infrastructure Deployment${NC}"
echo "=========================================="
echo ""

# Check prerequisites
echo "Checking prerequisites..."

# Check if Terraform is installed
if ! command -v terraform &> /dev/null; then
    echo -e "${RED}✗ Terraform is not installed${NC}"
    echo "Please install Terraform: https://www.terraform.io/downloads"
    exit 1
fi
echo -e "${GREEN}✓ Terraform installed${NC}"

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}✗ AWS CLI is not installed${NC}"
    echo "Please install AWS CLI: https://aws.amazon.com/cli/"
    exit 1
fi
echo -e "${GREEN}✓ AWS CLI installed${NC}"

# Check AWS credentials
if ! aws sts get-caller-identity &> /dev/null; then
    echo -e "${RED}✗ AWS credentials not configured${NC}"
    echo "Please configure AWS CLI: aws configure"
    exit 1
fi
echo -e "${GREEN}✓ AWS credentials configured${NC}"

# Check if terraform.tfvars exists
if [ ! -f "terraform.tfvars" ]; then
    echo -e "${YELLOW}⚠ terraform.tfvars not found${NC}"
    echo "Creating from example..."
    cp terraform.tfvars.example terraform.tfvars
    echo -e "${YELLOW}Please edit terraform.tfvars with your configuration${NC}"
    exit 1
fi
echo -e "${GREEN}✓ terraform.tfvars exists${NC}"

# Create placeholder Lambda package if it doesn't exist
if [ ! -f "lambda_placeholder.zip" ]; then
    echo ""
    echo "Creating placeholder Lambda package..."
    bash scripts/create_placeholder_lambda.sh
fi
echo -e "${GREEN}✓ Lambda placeholder package exists${NC}"

echo ""
echo "=========================================="
echo ""

# Initialize Terraform if needed
if [ ! -d ".terraform" ]; then
    echo "Initializing Terraform..."
    terraform init
    echo ""
fi

# Execute Terraform command
case $ACTION in
    plan)
        echo "Running Terraform plan..."
        terraform plan
        ;;
    apply)
        echo "Running Terraform apply..."
        terraform apply
        
        if [ $? -eq 0 ]; then
            echo ""
            echo -e "${GREEN}=========================================="
            echo "Infrastructure deployed successfully!"
            echo "==========================================${NC}"
            echo ""
            echo "Next steps:"
            echo "1. Deploy Lambda function code (see README.md)"
            echo "2. Configure Secrets Manager (JWT secret, Beckn keys)"
            echo "3. Enable Amazon Bedrock models"
            echo "4. Test API endpoints"
            echo ""
            echo "API Gateway URL:"
            terraform output api_gateway_url
        fi
        ;;
    destroy)
        echo -e "${RED}WARNING: This will destroy all infrastructure!${NC}"
        read -p "Are you sure? (yes/no): " confirm
        if [ "$confirm" = "yes" ]; then
            echo "Running Terraform destroy..."
            terraform destroy
        else
            echo "Destroy cancelled"
        fi
        ;;
    *)
        echo -e "${RED}Invalid action: $ACTION${NC}"
        echo "Usage: $0 [plan|apply|destroy]"
        exit 1
        ;;
esac
