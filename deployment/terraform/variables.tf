variable "location" {
  description = "geographic location of the Azure resources"
  default     = "westeurope"
  type        = string
}

variable "aws_region" {
  description = "geographic location of the AWS resources"
  default     = "us-east-1"
  type        = string
}

variable "aws_user" {
  description = "name of the AWS user being created"
  default     = "ion-demo-aws-user"
  type        = string
}

variable "environment" {
  description = "identifying string that is used as prefix in all azure resources"
}


variable "backend_account_name" {
  type        = string
  description = "A storage account where the Terraform state and certificates etc. are stored"
  default     = "edcstate"
}

variable "backend_account_key" {
  type        = string
  description = "Access key of the storage account that holds the terraform state and the certificate file share."
}

variable "CERTIFICATE" {
  type        = string
  description = "PEM-encoded content of the private key file, that is used to secure the primary azure app SP"
}

variable "docker_repo_password" {
  type = string
}

variable "docker_repo_username" {
  type = string
}

variable "docker_repo_url" {
  type    = string
  default = "ghcr.io"
}
# unique name for the consumer connector
variable "consumer-name" {
  type    = string
  default = "consumer"
}

# unique name for the provider connector
variable "provider-name" {
  type    = string
  default = "provider"
}

# unique name for the registration service
variable "regsvc-name" {
  type    = string
  default = "regsvc"
}

# partition key for the asset index based on cosmosdb
variable "asset-index-partkey" {
  default = "assetIndexPartition"
}