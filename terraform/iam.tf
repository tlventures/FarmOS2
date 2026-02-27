# Lambda Execution Role
resource "aws_iam_role" "lambda_execution" {
  name = "${var.project_name}-lambda-execution-${var.environment}"
  
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
  
  tags = {
    Name = "${var.project_name}-lambda-execution-${var.environment}"
  }
}

# Lambda Basic Execution Policy
resource "aws_iam_role_policy_attachment" "lambda_basic_execution" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# Lambda DynamoDB Access Policy
resource "aws_iam_policy" "lambda_dynamodb" {
  name        = "${var.project_name}-lambda-dynamodb-${var.environment}"
  description = "Allow Lambda functions to access DynamoDB tables"
  
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
          "dynamodb:DeleteItem",
          "dynamodb:Query",
          "dynamodb:Scan",
          "dynamodb:BatchGetItem",
          "dynamodb:BatchWriteItem"
        ]
        Resource = [
          aws_dynamodb_table.users.arn,
          aws_dynamodb_table.user_profiles.arn,
          aws_dynamodb_table.diagnoses.arn,
          aws_dynamodb_table.transactions.arn,
          aws_dynamodb_table.provider_ratings.arn,
          aws_dynamodb_table.telemetry_events.arn,
          aws_dynamodb_table.treatment_recommendations.arn,
          aws_dynamodb_table.beckn_search_cache.arn,
          aws_dynamodb_table.sync_queue.arn,
          "${aws_dynamodb_table.users.arn}/index/*",
          "${aws_dynamodb_table.user_profiles.arn}/index/*",
          "${aws_dynamodb_table.diagnoses.arn}/index/*",
          "${aws_dynamodb_table.transactions.arn}/index/*",
          "${aws_dynamodb_table.provider_ratings.arn}/index/*",
          "${aws_dynamodb_table.sync_queue.arn}/index/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_dynamodb" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = aws_iam_policy.lambda_dynamodb.arn
}

# Lambda S3 Access Policy
resource "aws_iam_policy" "lambda_s3" {
  name        = "${var.project_name}-lambda-s3-${var.environment}"
  description = "Allow Lambda functions to access S3 buckets"
  
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject",
          "s3:ListBucket"
        ]
        Resource = [
          aws_s3_bucket.images.arn,
          "${aws_s3_bucket.images.arn}/*",
          aws_s3_bucket.models.arn,
          "${aws_s3_bucket.models.arn}/*",
          aws_s3_bucket.audio.arn,
          "${aws_s3_bucket.audio.arn}/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject"
        ]
        Resource = [
          "${aws_s3_bucket.lambda_deployments.arn}/*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_s3" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = aws_iam_policy.lambda_s3.arn
}

# Lambda Bedrock Access Policy
resource "aws_iam_policy" "lambda_bedrock" {
  name        = "${var.project_name}-lambda-bedrock-${var.environment}"
  description = "Allow Lambda functions to access Amazon Bedrock"
  
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "bedrock:InvokeModel",
          "bedrock:InvokeModelWithResponseStream"
        ]
        Resource = [
          "arn:aws:bedrock:*::foundation-model/anthropic.claude-3-sonnet-20240229-v1:0",
          "arn:aws:bedrock:*::foundation-model/amazon.titan-embed-text-v1"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_bedrock" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = aws_iam_policy.lambda_bedrock.arn
}

# Lambda Transcribe/Polly Access Policy
resource "aws_iam_policy" "lambda_voice_services" {
  name        = "${var.project_name}-lambda-voice-services-${var.environment}"
  description = "Allow Lambda functions to access Transcribe and Polly"
  
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "transcribe:StartTranscriptionJob",
          "transcribe:GetTranscriptionJob",
          "transcribe:DeleteTranscriptionJob"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "polly:SynthesizeSpeech"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_voice_services" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = aws_iam_policy.lambda_voice_services.arn
}

# Lambda SNS Access Policy (for SMS OTP)
resource "aws_iam_policy" "lambda_sns" {
  name        = "${var.project_name}-lambda-sns-${var.environment}"
  description = "Allow Lambda functions to send SMS via SNS"
  
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "sns:Publish"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_sns" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = aws_iam_policy.lambda_sns.arn
}

# Lambda Secrets Manager Access Policy
resource "aws_iam_policy" "lambda_secrets" {
  name        = "${var.project_name}-lambda-secrets-${var.environment}"
  description = "Allow Lambda functions to access Secrets Manager"
  
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "secretsmanager:GetSecretValue"
        ]
        Resource = "arn:aws:secretsmanager:${var.aws_region}:*:secret:${var.project_name}/*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "lambda_secrets" {
  role       = aws_iam_role.lambda_execution.name
  policy_arn = aws_iam_policy.lambda_secrets.arn
}

# API Gateway CloudWatch Logs Role
resource "aws_iam_role" "api_gateway_cloudwatch" {
  name = "${var.project_name}-api-gateway-cloudwatch-${var.environment}"
  
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "apigateway.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "api_gateway_cloudwatch" {
  role       = aws_iam_role.api_gateway_cloudwatch.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonAPIGatewayPushToCloudWatchLogs"
}
