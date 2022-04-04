# Configure the Azure provider
terraform {
  # comment out this object if you want to use local state only
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 2.85.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.9.0"
    }
    aws = {
      source  = "hashicorp/aws"
      version = "3.65.0"
    }
    http = {
      source  = "hashicorp/http"
      version = "2.1.0"
    }
  }
}

provider "azurerm" {
  features {
    key_vault {
      purge_soft_delete_on_destroy    = true
      recover_soft_deleted_key_vaults = false
    }
  }
}
provider "azuread" {
  # Configuration options
}

data "azurerm_client_config" "current" {}
data "azurerm_subscription" "primary" {}


resource "azurerm_resource_group" "core-resourcegroup" {
  name     = "${var.environment}-resources"
  location = var.location
}

# App registration for the primary identity
data "azuread_application" "demo-app-id" {
  application_id = var.application_id
}
data "azuread_service_principal" "main-app-sp" {
  application_id = var.application_id
}

resource "azuread_application_certificate" "demo-main-identity-cert" {
  type                  = "AsymmetricX509Cert"
  application_object_id = data.azuread_application.demo-app-id.id
  value                 = file("${var.CERTIFICATE}.pem")
  end_date_relative     = "2400h"
}

data "azurerm_container_registry" "main" {
  name                = var.acr
  resource_group_name = var.acr_rg
}
locals {
  docker_repo_url      = data.azurerm_container_registry.main.login_server
  docker_repo_username = data.azurerm_container_registry.main.admin_username
  docker_repo_password = data.azurerm_container_registry.main.admin_password
}

resource "azurerm_storage_account" "data" {
  name                     = "sa${var.environment}prov"
  location                 = azurerm_resource_group.core-resourcegroup.location
  resource_group_name      = azurerm_resource_group.core-resourcegroup.name
  account_tier             = "Standard"
  account_replication_type = "LRS"
}
resource "azurerm_storage_share" "cert" {
  name                 = "certificates"
  storage_account_name = azurerm_storage_account.data.name
  quota                = 50
}
resource "azurerm_storage_share_file" "cert" {
  name             = "cert.pfx"
  storage_share_id = azurerm_storage_share.cert.id
  source           = "${var.CERTIFICATE}.pfx"
}
locals {
  share_account_key   = azurerm_storage_account.data.primary_access_key
  share_account_name  = azurerm_storage_account.data.name
  share_account_share = azurerm_storage_share.cert.name
}

locals {
  nodes_json = <<-EOT
    [
    {
      "name": "consumer",
      "url": "http://${var.environment}-${var.consumer-name}.${var.location}.azurecontainer.io:8282",
      "supportedProtocols": [
        "ids-multipart"
      ]
    },
    {
      "name": "provider",
      "url": "http://${var.environment}-${var.provider-name}.${var.location}.azurecontainer.io:8282",
      "supportedProtocols": [
        "ids-multipart"
      ]
    },
    {
      "name": "connector3",
      "url": "http://${var.environment}-connector3.${var.location}.azurecontainer.io:8282",
      "supportedProtocols": [
        "ids-multipart"
      ]
    }
  ]
EOT
}

resource "null_resource" "connector_files" {
  depends_on = [
    azurerm_storage_share_file.cert,
  ]
}

# Keyvault
resource "azurerm_key_vault" "main-vault" {
  name                        = "${var.environment}-vault"
  location                    = azurerm_resource_group.core-resourcegroup.location
  resource_group_name         = azurerm_resource_group.core-resourcegroup.name
  enabled_for_disk_encryption = false
  tenant_id                   = data.azurerm_client_config.current.tenant_id
  soft_delete_retention_days  = 7
  purge_protection_enabled    = false

  sku_name                  = "standard"
  enable_rbac_authorization = true

}

# Role assignment so that the primary identity may access the vault
resource "azurerm_role_assignment" "primary-id" {
  scope                = azurerm_key_vault.main-vault.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = data.azuread_service_principal.main-app-sp.object_id
}

# Role assignment that the primary identity may provision/deprovision azure resources
resource "azurerm_role_assignment" "primary-id-arm" {
  principal_id         = data.azuread_service_principal.main-app-sp.object_id
  scope                = data.azurerm_subscription.primary.id
  role_definition_name = "Contributor"
}

# Role assignment so that the currently logged in user may access the vault, needed to add secrets
resource "azurerm_role_assignment" "current-user-secretsofficer" {
  scope                = azurerm_key_vault.main-vault.id
  role_definition_name = "Key Vault Secrets Officer"
  principal_id         = data.azurerm_client_config.current.object_id
}
# Role assignment so that the currently logged in user may access the vault, needed to add keys
resource "azurerm_role_assignment" "current-user-cryptoofficer" {
  scope                = azurerm_key_vault.main-vault.id
  role_definition_name = "Key Vault Crypto Officer"
  principal_id         = data.azurerm_client_config.current.object_id
}


# registration service = ion crawler
#resource "azurerm_container_group" "registration-service" {
#  name                = "${var.environment}-${var.regsvc-name}"
#  location            = azurerm_resource_group.core-resourcegroup.location
#  resource_group_name = azurerm_resource_group.core-resourcegroup.name
#  os_type             = "Linux"
#  ip_address_type     = "public"
#  dns_name_label      = "${var.environment}-${var.regsvc-name}"
#  image_registry_credential {
#    password = var.docker_repo_password
#    server   = var.docker_repo_url
#    username = var.docker_repo_username
#  }
#  container {
#    cpu   = 2
#    image = "${var.docker_repo_url}/paullatzelsperger/edc-showcase/regsvc:latest"
#    //    image  = "paullatzelsperger/gx-reg-svc:latest"
#    memory = "2"
#    name   = var.regsvc-name
#
#    ports {
#      port     = 8181
#      protocol = "TCP"
#    }
#
#    environment_variables = {
#      CLIENTID          = azuread_application.demo-app-id.application_id,
#      TENANTID          = data.azurerm_client_config.current.tenant_id,
#      VAULTNAME         = azurerm_key_vault.main-vault.name,
#      CONNECTOR_NAME    = var.regsvc-name
#      TOPIC_NAME        = azurerm_eventgrid_topic.control-topic.name
#      TOPIC_ENDPOINT    = azurerm_eventgrid_topic.control-topic.endpoint
#      ION_URL           = "http://gx-ion-node.westeurope.cloudapp.azure.com:3000/"
#      LOADER_BATCH_SIZE = 2
#    }
#
#    volume {
#      mount_path           = "/cert"
#      name                 = "certificates"
#      share_name           = "certificates"
#      storage_account_key  = var.backend_account_key
#      storage_account_name = var.backend_account_name
#      read_only            = true
#    }
#  }
#}

# connector that acts as data provider
resource "azurerm_container_group" "provider-connector" {
  name                = "${var.environment}-${var.provider-name}"
  location            = azurerm_resource_group.core-resourcegroup.location
  resource_group_name = azurerm_resource_group.core-resourcegroup.name
  os_type             = "Linux"
  ip_address_type     = "public"
  dns_name_label      = "${var.environment}-${var.provider-name}"
  image_registry_credential {
    password = local.docker_repo_password
    server   = local.docker_repo_url
    username = local.docker_repo_username
  }
  restart_policy = "Never"
  container {
    cpu    = 2
    image  = "${local.docker_repo_url}/paullatzelsperger/edc-showcase/connector:latest"
    memory = "2"
    name   = var.provider-name

    ports {
      port     = 8181
      protocol = "TCP"
    }
    ports {
      port     = 8282
      protocol = "TCP"
    }

    environment_variables = {
      NODES_JSON          = local.nodes_json
      IDS_WEBHOOK_ADDRESS = "http://${var.environment}-${var.provider-name}.${var.location}.azurecontainer.io:8282"
      CLIENTID            = data.azuread_service_principal.main-app-sp.application_id,
      TENANTID            = data.azurerm_client_config.current.tenant_id,
      VAULTNAME           = azurerm_key_vault.main-vault.name,
      CONNECTOR_NAME      = var.provider-name
      TOPIC_NAME          = azurerm_eventgrid_topic.control-topic.name
      TOPIC_ENDPOINT      = azurerm_eventgrid_topic.control-topic.endpoint
      DID_URL             = "did:web:${azurerm_storage_account.main-blobstore.primary_web_host}:provider"
      LOADER_BATCH_SIZE   = 1
      DOH_SERVER          = "https://cloudflare-dns.com/dns-query"
      COSMOS_ACCOUNT      = azurerm_cosmosdb_account.showcase-cosmos-account.name
      COSMOS_DB           = azurerm_cosmosdb_sql_database.asset-index-db.name
      COSMOS_PARTKEY      = azurerm_cosmosdb_sql_container.provider-assetindex-container.partition_key_path
      COSMOS_CONTAINER    = azurerm_cosmosdb_sql_container.provider-assetindex-container.name

      CDS_DATABASE  = azurerm_cosmosdb_sql_database.contractdefinition-store-db.name
      CDS_CONTAINER = azurerm_cosmosdb_sql_container.provider-contractdefstore-container.name

      CNS_DATABASE  = azurerm_cosmosdb_sql_database.contractnegotiation-store-db.name
      CNS_CONTAINER = azurerm_cosmosdb_sql_container.provider-contractnegotiation-container.name
    }

    volume {
      mount_path           = "/cert"
      name                 = "certificates"
      share_name           = local.share_account_share
      storage_account_key  = local.share_account_key
      storage_account_name = local.share_account_name
      read_only            = true
    }
  }
  depends_on = [
    null_resource.connector_files,
  ]
}

# connector that acts as data consumer
resource "azurerm_container_group" "consumer-connector" {
  name                = "${var.environment}-${var.consumer-name}"
  location            = azurerm_resource_group.core-resourcegroup.location
  resource_group_name = azurerm_resource_group.core-resourcegroup.name
  os_type             = "Linux"
  ip_address_type     = "public"
  dns_name_label      = "${var.environment}-${var.consumer-name}"
  image_registry_credential {
    password = local.docker_repo_password
    server   = local.docker_repo_url
    username = local.docker_repo_username
  }
  restart_policy = "Never"
  container {
    cpu    = 2
    image  = "${local.docker_repo_url}/paullatzelsperger/edc-showcase/connector:latest"
    memory = "2"
    name   = var.consumer-name

    ports {
      port     = 8181
      protocol = "TCP"
    }
    ports {
      port     = 8282
      protocol = "TCP"
    }

    environment_variables = {
      NODES_JSON          = local.nodes_json
      IDS_WEBHOOK_ADDRESS = "http://${var.environment}-${var.consumer-name}.${var.location}.azurecontainer.io:8282"
      CLIENTID            = data.azuread_service_principal.main-app-sp.application_id,
      TENANTID            = data.azurerm_client_config.current.tenant_id,
      VAULTNAME           = azurerm_key_vault.main-vault.name,
      CONNECTOR_NAME      = var.consumer-name
      TOPIC_NAME          = azurerm_eventgrid_topic.control-topic.name
      TOPIC_ENDPOINT      = azurerm_eventgrid_topic.control-topic.endpoint
      DID_URL             = "did:web:${azurerm_storage_account.main-blobstore.primary_web_host}:consumer"
      DOH_SERVER          = "https://cloudflare-dns.com/dns-query"
      LOADER_BATCH_SIZE   = 2
      COSMOS_ACCOUNT      = azurerm_cosmosdb_account.showcase-cosmos-account.name
      COSMOS_DB           = azurerm_cosmosdb_sql_database.asset-index-db.name
      COSMOS_PARTKEY      = azurerm_cosmosdb_sql_container.consumer-assetindex-container.partition_key_path
      COSMOS_CONTAINER    = azurerm_cosmosdb_sql_container.consumer-assetindex-container.name

      CDS_DATABASE  = azurerm_cosmosdb_sql_database.contractdefinition-store-db.name
      CDS_CONTAINER = azurerm_cosmosdb_sql_container.consumer-contractdefstore-container.name

      CNS_DATABASE  = azurerm_cosmosdb_sql_database.contractnegotiation-store-db.name
      CNS_CONTAINER = azurerm_cosmosdb_sql_container.consumer-contractnegotiation-container.name
    }

    volume {
      mount_path           = "/cert"
      name                 = "certificates"
      share_name           = local.share_account_share
      storage_account_key  = local.share_account_key
      storage_account_name = local.share_account_name
      read_only            = true
    }
  }
  depends_on = [
    null_resource.connector_files,
  ]
}

# arbitrary third connector that hosts its did on a static site
resource "azurerm_container_group" "connector3" {
  name                = "${var.environment}-connector3"
  location            = azurerm_resource_group.core-resourcegroup.location
  resource_group_name = azurerm_resource_group.core-resourcegroup.name
  os_type             = "Linux"
  ip_address_type     = "public"
  dns_name_label      = "${var.environment}-connector3"
  image_registry_credential {
    password = local.docker_repo_password
    server   = local.docker_repo_url
    username = local.docker_repo_username
  }
  restart_policy = "Never"
  container {
    cpu    = 2
    image  = "${local.docker_repo_url}/paullatzelsperger/edc-showcase/connector:latest"
    memory = "2"
    name   = "connector3"

    ports {
      port     = 8181
      protocol = "TCP"
    }
    ports {
      port     = 8282
      protocol = "TCP"
    }

    environment_variables = {
      NODES_JSON          = local.nodes_json
      IDS_WEBHOOK_ADDRESS = "http://${var.environment}-connector3.${var.location}.azurecontainer.io:8282"
      CLIENTID            = data.azuread_service_principal.main-app-sp.application_id,
      TENANTID            = data.azurerm_client_config.current.tenant_id,
      VAULTNAME           = azurerm_key_vault.main-vault.name,
      CONNECTOR_NAME      = "connector3"
      TOPIC_NAME          = azurerm_eventgrid_topic.control-topic.name
      TOPIC_ENDPOINT      = azurerm_eventgrid_topic.control-topic.endpoint
      DID_URL             = "did:web:${azurerm_storage_account.main-blobstore.primary_web_host}:connector3"
      DOH_SERVER          = "https://cloudflare-dns.com/dns-query"
      LOADER_BATCH_SIZE   = 2
      COSMOS_ACCOUNT      = azurerm_cosmosdb_account.showcase-cosmos-account.name
      COSMOS_DB           = azurerm_cosmosdb_sql_database.asset-index-db.name
      COSMOS_PARTKEY      = azurerm_cosmosdb_sql_container.connector3-assetindex-container.partition_key_path
      COSMOS_CONTAINER    = azurerm_cosmosdb_sql_container.connector3-assetindex-container.name

      CDS_DATABASE  = azurerm_cosmosdb_sql_database.contractdefinition-store-db.name
      CDS_CONTAINER = azurerm_cosmosdb_sql_container.connector3-contractdefstore-container.name

      CNS_DATABASE  = azurerm_cosmosdb_sql_database.contractnegotiation-store-db.name
      CNS_CONTAINER = azurerm_cosmosdb_sql_container.connector3-contractnegotiation-container.name
    }

    volume {
      mount_path           = "/cert"
      name                 = "certificates"
      share_name           = local.share_account_share
      storage_account_key  = local.share_account_key
      storage_account_name = local.share_account_name
      read_only            = true
    }
  }
  depends_on = [
    null_resource.connector_files,
  ]
}
