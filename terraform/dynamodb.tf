# Users Table
resource "aws_dynamodb_table" "users" {
  name           = "${var.project_name}-users-${var.environment}"
  billing_mode   = var.dynamodb_billing_mode
  hash_key       = "user_id"
  
  attribute {
    name = "user_id"
    type = "S"
  }
  
  attribute {
    name = "phone_number"
    type = "S"
  }
  
  global_secondary_index {
    name            = "PhoneNumberIndex"
    hash_key        = "phone_number"
    projection_type = "ALL"
  }
  
  point_in_time_recovery {
    enabled = true
  }
  
  server_side_encryption {
    enabled = true
  }
  
  tags = {
    Name = "${var.project_name}-users-${var.environment}"
  }
}

# User Profiles Table
resource "aws_dynamodb_table" "user_profiles" {
  name           = "${var.project_name}-user-profiles-${var.environment}"
  billing_mode   = var.dynamodb_billing_mode
  hash_key       = "user_id"
  
  attribute {
    name = "user_id"
    type = "S"
  }
  
  attribute {
    name = "state"
    type = "S"
  }
  
  attribute {
    name = "district"
    type = "S"
  }
  
  global_secondary_index {
    name            = "LocationIndex"
    hash_key        = "state"
    range_key       = "district"
    projection_type = "ALL"
  }
  
  point_in_time_recovery {
    enabled = true
  }
  
  server_side_encryption {
    enabled = true
  }
  
  tags = {
    Name = "${var.project_name}-user-profiles-${var.environment}"
  }
}

# Diagnoses Table
resource "aws_dynamodb_table" "diagnoses" {
  name           = "${var.project_name}-diagnoses-${var.environment}"
  billing_mode   = var.dynamodb_billing_mode
  hash_key       = "user_id"
  range_key      = "timestamp"
  
  attribute {
    name = "user_id"
    type = "S"
  }
  
  attribute {
    name = "timestamp"
    type = "N"
  }
  
  attribute {
    name = "diagnosis_id"
    type = "S"
  }
  
  attribute {
    name = "disease_id"
    type = "S"
  }
  
  global_secondary_index {
    name            = "DiagnosisIdIndex"
    hash_key        = "diagnosis_id"
    projection_type = "ALL"
  }
  
  global_secondary_index {
    name            = "DiseaseIndex"
    hash_key        = "disease_id"
    range_key       = "timestamp"
    projection_type = "ALL"
  }
  
  point_in_time_recovery {
    enabled = true
  }
  
  server_side_encryption {
    enabled = true
  }
  
  tags = {
    Name = "${var.project_name}-diagnoses-${var.environment}"
  }
}

# Transactions Table
resource "aws_dynamodb_table" "transactions" {
  name           = "${var.project_name}-transactions-${var.environment}"
  billing_mode   = var.dynamodb_billing_mode
  hash_key       = "user_id"
  range_key      = "created_at"
  
  attribute {
    name = "user_id"
    type = "S"
  }
  
  attribute {
    name = "created_at"
    type = "N"
  }
  
  attribute {
    name = "transaction_id"
    type = "S"
  }
  
  attribute {
    name = "status"
    type = "S"
  }
  
  global_secondary_index {
    name            = "TransactionIdIndex"
    hash_key        = "transaction_id"
    projection_type = "ALL"
  }
  
  global_secondary_index {
    name            = "StatusIndex"
    hash_key        = "status"
    range_key       = "created_at"
    projection_type = "ALL"
  }
  
  point_in_time_recovery {
    enabled = true
  }
  
  server_side_encryption {
    enabled = true
  }
  
  tags = {
    Name = "${var.project_name}-transactions-${var.environment}"
  }
}

# Provider Ratings Table
resource "aws_dynamodb_table" "provider_ratings" {
  name           = "${var.project_name}-provider-ratings-${var.environment}"
  billing_mode   = var.dynamodb_billing_mode
  hash_key       = "provider_id"
  range_key      = "created_at"
  
  attribute {
    name = "provider_id"
    type = "S"
  }
  
  attribute {
    name = "created_at"
    type = "N"
  }
  
  attribute {
    name = "user_id"
    type = "S"
  }
  
  global_secondary_index {
    name            = "UserRatingsIndex"
    hash_key        = "user_id"
    range_key       = "created_at"
    projection_type = "ALL"
  }
  
  point_in_time_recovery {
    enabled = true
  }
  
  server_side_encryption {
    enabled = true
  }
  
  tags = {
    Name = "${var.project_name}-provider-ratings-${var.environment}"
  }
}

# Telemetry Events Table
resource "aws_dynamodb_table" "telemetry_events" {
  name           = "${var.project_name}-telemetry-events-${var.environment}"
  billing_mode   = var.dynamodb_billing_mode
  hash_key       = "event_type"
  range_key      = "timestamp"
  
  attribute {
    name = "event_type"
    type = "S"
  }
  
  attribute {
    name = "timestamp"
    type = "N"
  }
  
  ttl {
    attribute_name = "ttl"
    enabled        = true
  }
  
  point_in_time_recovery {
    enabled = true
  }
  
  server_side_encryption {
    enabled = true
  }
  
  tags = {
    Name = "${var.project_name}-telemetry-events-${var.environment}"
  }
}

# Treatment Recommendations Cache Table
resource "aws_dynamodb_table" "treatment_recommendations" {
  name           = "${var.project_name}-treatment-recommendations-${var.environment}"
  billing_mode   = var.dynamodb_billing_mode
  hash_key       = "disease_id"
  range_key      = "language"
  
  attribute {
    name = "disease_id"
    type = "S"
  }
  
  attribute {
    name = "language"
    type = "S"
  }
  
  ttl {
    attribute_name = "ttl"
    enabled        = true
  }
  
  point_in_time_recovery {
    enabled = true
  }
  
  server_side_encryption {
    enabled = true
  }
  
  tags = {
    Name = "${var.project_name}-treatment-recommendations-${var.environment}"
  }
}

# Beckn Search Cache Table
resource "aws_dynamodb_table" "beckn_search_cache" {
  name           = "${var.project_name}-beckn-search-cache-${var.environment}"
  billing_mode   = var.dynamodb_billing_mode
  hash_key       = "transaction_id"
  
  attribute {
    name = "transaction_id"
    type = "S"
  }
  
  ttl {
    attribute_name = "ttl"
    enabled        = true
  }
  
  point_in_time_recovery {
    enabled = true
  }
  
  server_side_encryption {
    enabled = true
  }
  
  tags = {
    Name = "${var.project_name}-beckn-search-cache-${var.environment}"
  }
}

# Sync Queue Table
resource "aws_dynamodb_table" "sync_queue" {
  name           = "${var.project_name}-sync-queue-${var.environment}"
  billing_mode   = var.dynamodb_billing_mode
  hash_key       = "user_id"
  range_key      = "timestamp"
  
  attribute {
    name = "user_id"
    type = "S"
  }
  
  attribute {
    name = "timestamp"
    type = "N"
  }
  
  attribute {
    name = "status"
    type = "S"
  }
  
  global_secondary_index {
    name            = "StatusIndex"
    hash_key        = "status"
    range_key       = "timestamp"
    projection_type = "ALL"
  }
  
  point_in_time_recovery {
    enabled = true
  }
  
  server_side_encryption {
    enabled = true
  }
  
  tags = {
    Name = "${var.project_name}-sync-queue-${var.environment}"
  }
}
