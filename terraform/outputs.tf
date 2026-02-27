output "api_gateway_url" {
  description = "API Gateway invoke URL"
  value       = aws_api_gateway_stage.main.invoke_url
}

output "api_gateway_id" {
  description = "API Gateway REST API ID"
  value       = aws_api_gateway_rest_api.main.id
}

output "lambda_function_names" {
  description = "Names of all Lambda functions"
  value = {
    auth                = aws_lambda_function.auth.function_name
    user_profile        = aws_lambda_function.user_profile.function_name
    sync                = aws_lambda_function.sync.function_name
    market_connector    = aws_lambda_function.market_connector.function_name
    telemetry           = aws_lambda_function.telemetry.function_name
    treatment_generator = aws_lambda_function.treatment_generator.function_name
    voice_interface     = aws_lambda_function.voice_interface.function_name
    presigned_url       = aws_lambda_function.presigned_url.function_name
  }
}

output "dynamodb_table_names" {
  description = "Names of all DynamoDB tables"
  value = {
    users                     = aws_dynamodb_table.users.name
    user_profiles             = aws_dynamodb_table.user_profiles.name
    diagnoses                 = aws_dynamodb_table.diagnoses.name
    transactions              = aws_dynamodb_table.transactions.name
    provider_ratings          = aws_dynamodb_table.provider_ratings.name
    telemetry_events          = aws_dynamodb_table.telemetry_events.name
    treatment_recommendations = aws_dynamodb_table.treatment_recommendations.name
    beckn_search_cache        = aws_dynamodb_table.beckn_search_cache.name
    sync_queue                = aws_dynamodb_table.sync_queue.name
  }
}

output "s3_bucket_names" {
  description = "Names of all S3 buckets"
  value = {
    images             = aws_s3_bucket.images.id
    models             = aws_s3_bucket.models.id
    audio              = aws_s3_bucket.audio.id
    lambda_deployments = aws_s3_bucket.lambda_deployments.id
  }
}

output "iam_role_arns" {
  description = "ARNs of IAM roles"
  value = {
    lambda_execution        = aws_iam_role.lambda_execution.arn
    api_gateway_cloudwatch = aws_iam_role.api_gateway_cloudwatch.arn
  }
}

output "cloudwatch_log_groups" {
  description = "CloudWatch log group names"
  value = {
    api_gateway            = aws_cloudwatch_log_group.api_gateway.name
    auth_lambda            = aws_cloudwatch_log_group.auth_lambda.name
    user_profile_lambda    = aws_cloudwatch_log_group.user_profile_lambda.name
    sync_lambda            = aws_cloudwatch_log_group.sync_lambda.name
    market_connector_lambda = aws_cloudwatch_log_group.market_connector_lambda.name
    telemetry_lambda       = aws_cloudwatch_log_group.telemetry_lambda.name
    treatment_generator_lambda = aws_cloudwatch_log_group.treatment_generator_lambda.name
    voice_interface_lambda = aws_cloudwatch_log_group.voice_interface_lambda.name
    presigned_url_lambda   = aws_cloudwatch_log_group.presigned_url_lambda.name
  }
}
