# CloudWatch Log Groups for Lambda Functions
resource "aws_cloudwatch_log_group" "auth_lambda" {
  name              = "/aws/lambda/${var.project_name}-auth-${var.environment}"
  retention_in_days = var.cloudwatch_log_retention_days
}

resource "aws_cloudwatch_log_group" "user_profile_lambda" {
  name              = "/aws/lambda/${var.project_name}-user-profile-${var.environment}"
  retention_in_days = var.cloudwatch_log_retention_days
}

resource "aws_cloudwatch_log_group" "sync_lambda" {
  name              = "/aws/lambda/${var.project_name}-sync-${var.environment}"
  retention_in_days = var.cloudwatch_log_retention_days
}

resource "aws_cloudwatch_log_group" "market_connector_lambda" {
  name              = "/aws/lambda/${var.project_name}-market-connector-${var.environment}"
  retention_in_days = var.cloudwatch_log_retention_days
}

resource "aws_cloudwatch_log_group" "telemetry_lambda" {
  name              = "/aws/lambda/${var.project_name}-telemetry-${var.environment}"
  retention_in_days = var.cloudwatch_log_retention_days
}

resource "aws_cloudwatch_log_group" "treatment_generator_lambda" {
  name              = "/aws/lambda/${var.project_name}-treatment-generator-${var.environment}"
  retention_in_days = var.cloudwatch_log_retention_days
}

resource "aws_cloudwatch_log_group" "voice_interface_lambda" {
  name              = "/aws/lambda/${var.project_name}-voice-interface-${var.environment}"
  retention_in_days = var.cloudwatch_log_retention_days
}

resource "aws_cloudwatch_log_group" "presigned_url_lambda" {
  name              = "/aws/lambda/${var.project_name}-presigned-url-${var.environment}"
  retention_in_days = var.cloudwatch_log_retention_days
}

# Auth Lambda Function
resource "aws_lambda_function" "auth" {
  function_name = "${var.project_name}-auth-${var.environment}"
  role          = aws_iam_role.lambda_execution.arn
  handler       = "auth.lambda_handler"
  runtime       = var.lambda_runtime
  timeout       = var.lambda_timeout
  memory_size   = var.lambda_memory_size
  
  # Placeholder for deployment package
  filename         = "lambda_placeholder.zip"
  source_code_hash = filebase64sha256("lambda_placeholder.zip")
  
  environment {
    variables = {
      ENVIRONMENT           = var.environment
      USERS_TABLE          = aws_dynamodb_table.users.name
      USER_PROFILES_TABLE  = aws_dynamodb_table.user_profiles.name
      JWT_SECRET_NAME      = "${var.project_name}/jwt-secret"
    }
  }
  
  depends_on = [
    aws_cloudwatch_log_group.auth_lambda
  ]
}

# User Profile Lambda Function
resource "aws_lambda_function" "user_profile" {
  function_name = "${var.project_name}-user-profile-${var.environment}"
  role          = aws_iam_role.lambda_execution.arn
  handler       = "user_profile.lambda_handler"
  runtime       = var.lambda_runtime
  timeout       = var.lambda_timeout
  memory_size   = var.lambda_memory_size
  
  filename         = "lambda_placeholder.zip"
  source_code_hash = filebase64sha256("lambda_placeholder.zip")
  
  environment {
    variables = {
      ENVIRONMENT          = var.environment
      USER_PROFILES_TABLE = aws_dynamodb_table.user_profiles.name
    }
  }
  
  depends_on = [
    aws_cloudwatch_log_group.user_profile_lambda
  ]
}

# Sync Lambda Function
resource "aws_lambda_function" "sync" {
  function_name = "${var.project_name}-sync-${var.environment}"
  role          = aws_iam_role.lambda_execution.arn
  handler       = "sync.lambda_handler"
  runtime       = var.lambda_runtime
  timeout       = 60  # Longer timeout for batch processing
  memory_size   = 1024  # More memory for batch operations
  
  filename         = "lambda_placeholder.zip"
  source_code_hash = filebase64sha256("lambda_placeholder.zip")
  
  environment {
    variables = {
      ENVIRONMENT              = var.environment
      DIAGNOSES_TABLE         = aws_dynamodb_table.diagnoses.name
      RATINGS_TABLE           = aws_dynamodb_table.provider_ratings.name
      SYNC_QUEUE_TABLE        = aws_dynamodb_table.sync_queue.name
      IMAGES_BUCKET           = aws_s3_bucket.images.id
    }
  }
  
  depends_on = [
    aws_cloudwatch_log_group.sync_lambda
  ]
}

# Market Connector Lambda Function
resource "aws_lambda_function" "market_connector" {
  function_name = "${var.project_name}-market-connector-${var.environment}"
  role          = aws_iam_role.lambda_execution.arn
  handler       = "market_connector.lambda_handler"
  runtime       = var.lambda_runtime
  timeout       = var.lambda_timeout
  memory_size   = var.lambda_memory_size
  
  filename         = "lambda_placeholder.zip"
  source_code_hash = filebase64sha256("lambda_placeholder.zip")
  
  environment {
    variables = {
      ENVIRONMENT              = var.environment
      TRANSACTIONS_TABLE      = aws_dynamodb_table.transactions.name
      BECKN_CACHE_TABLE       = aws_dynamodb_table.beckn_search_cache.name
      BECKN_GATEWAY_URL       = "https://beckn-gateway.ondc.org"
      BECKN_SUBSCRIBER_ID     = "${var.project_name}-${var.environment}"
    }
  }
  
  depends_on = [
    aws_cloudwatch_log_group.market_connector_lambda
  ]
}

# Telemetry Lambda Function
resource "aws_lambda_function" "telemetry" {
  function_name = "${var.project_name}-telemetry-${var.environment}"
  role          = aws_iam_role.lambda_execution.arn
  handler       = "telemetry.lambda_handler"
  runtime       = var.lambda_runtime
  timeout       = var.lambda_timeout
  memory_size   = var.lambda_memory_size
  
  filename         = "lambda_placeholder.zip"
  source_code_hash = filebase64sha256("lambda_placeholder.zip")
  
  environment {
    variables = {
      ENVIRONMENT          = var.environment
      TELEMETRY_TABLE     = aws_dynamodb_table.telemetry_events.name
    }
  }
  
  depends_on = [
    aws_cloudwatch_log_group.telemetry_lambda
  ]
}

# Treatment Generator Lambda Function (Bedrock)
resource "aws_lambda_function" "treatment_generator" {
  function_name = "${var.project_name}-treatment-generator-${var.environment}"
  role          = aws_iam_role.lambda_execution.arn
  handler       = "treatment_generator.lambda_handler"
  runtime       = var.lambda_runtime
  timeout       = 60  # Longer timeout for Bedrock API calls
  memory_size   = 512
  
  filename         = "lambda_placeholder.zip"
  source_code_hash = filebase64sha256("lambda_placeholder.zip")
  
  environment {
    variables = {
      ENVIRONMENT              = var.environment
      TREATMENT_CACHE_TABLE   = aws_dynamodb_table.treatment_recommendations.name
      BEDROCK_MODEL_ID        = "anthropic.claude-3-sonnet-20240229-v1:0"
    }
  }
  
  depends_on = [
    aws_cloudwatch_log_group.treatment_generator_lambda
  ]
}

# Voice Interface Lambda Function (Transcribe/Polly)
resource "aws_lambda_function" "voice_interface" {
  function_name = "${var.project_name}-voice-interface-${var.environment}"
  role          = aws_iam_role.lambda_execution.arn
  handler       = "voice_interface.lambda_handler"
  runtime       = var.lambda_runtime
  timeout       = 60  # Longer timeout for transcription
  memory_size   = 512
  
  filename         = "lambda_placeholder.zip"
  source_code_hash = filebase64sha256("lambda_placeholder.zip")
  
  environment {
    variables = {
      ENVIRONMENT     = var.environment
      AUDIO_BUCKET   = aws_s3_bucket.audio.id
    }
  }
  
  depends_on = [
    aws_cloudwatch_log_group.voice_interface_lambda
  ]
}

# Presigned URL Lambda Function
resource "aws_lambda_function" "presigned_url" {
  function_name = "${var.project_name}-presigned-url-${var.environment}"
  role          = aws_iam_role.lambda_execution.arn
  handler       = "presigned_url.lambda_handler"
  runtime       = var.lambda_runtime
  timeout       = var.lambda_timeout
  memory_size   = 256
  
  filename         = "lambda_placeholder.zip"
  source_code_hash = filebase64sha256("lambda_placeholder.zip")
  
  environment {
    variables = {
      ENVIRONMENT     = var.environment
      IMAGES_BUCKET  = aws_s3_bucket.images.id
    }
  }
  
  depends_on = [
    aws_cloudwatch_log_group.presigned_url_lambda
  ]
}
