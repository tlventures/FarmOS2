terraform {
  required_version = ">= 1.0"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
  
  backend "s3" {
    bucket = "agriedge-terraform-state"
    key    = "infrastructure/terraform.tfstate"
    region = "ap-south-1"
    encrypt = true
  }
}

provider "aws" {
  region = var.aws_region
  
  default_tags {
    tags = {
      Project     = "AgriEdge-Link"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}
