#!/bin/bash

# Script to create placeholder Lambda deployment package
# This is required before running terraform apply

set -e

echo "Creating placeholder Lambda deployment package..."

# Create temporary directory
TEMP_DIR=$(mktemp -d)
cd "$TEMP_DIR"

# Create placeholder Python file
cat > lambda_placeholder.py << 'EOF'
def lambda_handler(event, context):
    """
    Placeholder Lambda function
    This will be replaced with actual implementation after infrastructure is created
    """
    return {
        'statusCode': 200,
        'body': 'Placeholder function - deploy actual code after infrastructure setup'
    }
EOF

# Create ZIP file
zip lambda_placeholder.zip lambda_placeholder.py

# Move to terraform directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TERRAFORM_DIR="$(dirname "$SCRIPT_DIR")"
mv lambda_placeholder.zip "$TERRAFORM_DIR/"

# Cleanup
cd "$TERRAFORM_DIR"
rm -rf "$TEMP_DIR"

echo "✓ Placeholder Lambda package created: lambda_placeholder.zip"
