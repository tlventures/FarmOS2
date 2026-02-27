.PHONY: help init plan apply destroy clean validate format

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

init: ## Initialize Terraform
	@echo "Initializing Terraform..."
	@bash scripts/create_placeholder_lambda.sh
	@terraform init

plan: ## Run Terraform plan
	@echo "Running Terraform plan..."
	@terraform plan

apply: ## Apply Terraform configuration
	@echo "Applying Terraform configuration..."
	@bash scripts/deploy.sh apply

destroy: ## Destroy Terraform infrastructure
	@echo "Destroying Terraform infrastructure..."
	@bash scripts/deploy.sh destroy

validate: ## Validate Terraform configuration
	@echo "Validating Terraform configuration..."
	@terraform validate

format: ## Format Terraform files
	@echo "Formatting Terraform files..."
	@terraform fmt -recursive

clean: ## Clean Terraform files
	@echo "Cleaning Terraform files..."
	@rm -rf .terraform
	@rm -f .terraform.lock.hcl
	@rm -f terraform.tfstate*
	@rm -f lambda_placeholder.zip

output: ## Show Terraform outputs
	@terraform output

state-list: ## List Terraform state resources
	@terraform state list

docs: ## Generate documentation
	@echo "Terraform configuration documentation:"
	@echo ""
	@echo "See README.md for detailed documentation"
