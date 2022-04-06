variable "location" {
  description = "geographic location of the Azure resources"
  default     = "northeurope"
  type        = string
}

variable "aws_region" {
  description = "geographic location of the AWS resources"
  default     = "us-east-1"
  type        = string
}

variable "aws_user" {
  description = "name of the AWS user being created"
  default     = "edc-showcase-aws-user"
  type        = string
}

variable "environment" {
  description = "identifying string that is used as prefix in all azure resources"
}

variable "acr" {
}

variable "acr_rg" {
}

variable "application_id" {
}

variable "CERTIFICATE" {
  type        = string
  description = "Base name of two local files (.pem and .pfx) both containing the private key, that is used to secure the primary azure app SP"
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
