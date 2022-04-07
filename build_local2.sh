#! /bin/bash

set -euxo pipefail

cwd=$(pwd)

# Create a service principal to be used by the application
test -s secrets || az ad sp create-for-rbac --skip-assignment -o json > secrets
appId=$(jq -r .appId secrets)
tenant=$(jq -r .tenant secrets)
password=$(jq -r .password secrets)

ACR_NAME=ageramvdacr
RES_GROUP=$ACR_NAME # Resource Group name
az group create --resource-group $RES_GROUP --location westeurope -o none
az acr create --resource-group $RES_GROUP --name $ACR_NAME --sku Standard --location westeurope --admin-enabled -o none


# 1. build java modules
echo "building JAR file"
./gradlew clean shadowJar

# 1. build docker image(s)
cd launchers/connector 
echo "building connector docker image"
az acr build --registry $ACR_NAME --image paullatzelsperger/edc-showcase/connector:latest  .
cd "$cwd" 


# 1. setup terraform
TERRAFORM_STATE_STORAGE_RESOURCE_GROUP=ageramvdtf
TERRAFORM_STATE_STORAGE_ACCOUNT=ageramvdtf
TERRAFORM_STATE_STORAGE_CONTAINER=ageramvdtf
TERRAFORM_STATE_STORAGE_BLOB=ageramvdtf
TERRAFORM_STATE_STORAGE_LOCATION=westeurope

az group create --name "$TERRAFORM_STATE_STORAGE_RESOURCE_GROUP" --location "$TERRAFORM_STATE_STORAGE_LOCATION" -o none
az storage account create --resource-group "$TERRAFORM_STATE_STORAGE_RESOURCE_GROUP" --name "$TERRAFORM_STATE_STORAGE_ACCOUNT" -o none
az storage container create --name "$TERRAFORM_STATE_STORAGE_CONTAINER" --account-name "$TERRAFORM_STATE_STORAGE_ACCOUNT" --auth-mode login -o none

# 2. apply terraform
cd deployment/terraform/ 
echo "prepare terraform"
cat > generated_backend.tf <<EOF
terraform {
  backend "azurerm" {
    resource_group_name  = "$TERRAFORM_STATE_STORAGE_RESOURCE_GROUP"
    storage_account_name = "$TERRAFORM_STATE_STORAGE_ACCOUNT"
    container_name       = "$TERRAFORM_STATE_STORAGE_CONTAINER"
    key                  = "$TERRAFORM_STATE_STORAGE_BLOB"
  }
}
EOF

terraform init

export TF_VAR_CERTIFICATE=../cert
export TF_VAR_environment=ageramvd
export TF_VAR_acr=$ACR_NAME
export TF_VAR_acr_rg=$RES_GROUP
export TF_VAR_application_id=$appId

echo "apply terraform"
terraform apply -auto-approve
