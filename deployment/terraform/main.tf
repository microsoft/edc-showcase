# Configure the Azure provider
terraform {
  # comment out this object if you want to use local state only
  backend "azurerm" {
    resource_group_name  = "edc-infrastructure"
    storage_account_name = "edcstate"
    container_name       = "terraform-state-ion-demo"
    key                  = "terraform.state"
  }
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = ">= 2.78.0"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "2.4.0"
    }
    aws     = {
      source  = "hashicorp/aws"
      version = "3.60.0"
    }
    http    = {
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
resource "azuread_application" "demo-app-id" {
  display_name     = "PrimaryIdentity-${var.environment}"
  sign_in_audience = "AzureADMyOrg"
}

resource "azuread_application_certificate" "demo-main-identity-cert" {
  type                  = "AsymmetricX509Cert"
  application_object_id = azuread_application.demo-app-id.id
  value                 = var.CERTIFICATE
  end_date_relative     = "2400h"
}

resource "azuread_service_principal" "main-app-sp" {
  application_id               = azuread_application.demo-app-id.application_id
  app_role_assignment_required = false
  tags                         = [
    "terraform"
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
  principal_id         = azuread_service_principal.main-app-sp.object_id
}

# Role assignment that the primary identity may provision/deprovision azure resources
resource "azurerm_role_assignment" "primary-id-arm" {
  principal_id         = azuread_service_principal.main-app-sp.object_id
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
resource "azurerm_container_group" "registration-service" {
  name                = "${var.environment}-${var.regsvc-name}"
  location            = azurerm_resource_group.core-resourcegroup.location
  resource_group_name = azurerm_resource_group.core-resourcegroup.name
  os_type             = "Linux"
  ip_address_type     = "public"
  dns_name_label      = "${var.environment}-${var.regsvc-name}"
  image_registry_credential {
    password = var.docker_repo_password
    server   = var.docker_repo_url
    username = var.docker_repo_username
  }
  container {
    cpu    = 2
    image  = "${var.docker_repo_url}/paullatzelsperger/ion-demo/regsvc:latest"
    //    image  = "paullatzelsperger/gx-reg-svc:latest"
    memory = "2"
    name   = var.regsvc-name

    ports {
      port     = 8181
      protocol = "TCP"
    }

    environment_variables = {
      CLIENTID          = azuread_application.demo-app-id.application_id,
      TENANTID          = data.azurerm_client_config.current.tenant_id,
      VAULTNAME         = azurerm_key_vault.main-vault.name,
      CONNECTOR_NAME    = var.regsvc-name
      TOPIC_NAME        = azurerm_eventgrid_topic.control-topic.name
      TOPIC_ENDPOINT    = azurerm_eventgrid_topic.control-topic.endpoint
      ION_URL           = "http://gx-ion-node.westeurope.cloudapp.azure.com:3000/"
      LOADER_BATCH_SIZE = 2
    }

    volume {
      mount_path           = "/cert"
      name                 = "certificates"
      share_name           = "certificates"
      storage_account_key  = var.backend_account_key
      storage_account_name = var.backend_account_name
      read_only            = true
    }
  }
}

# connector that acts as data provider
resource "azurerm_container_group" "provider-connector" {
  name                = "${var.environment}-${var.provider-name}"
  location            = azurerm_resource_group.core-resourcegroup.location
  resource_group_name = azurerm_resource_group.core-resourcegroup.name
  os_type             = "Linux"
  ip_address_type     = "public"
  dns_name_label      = "${var.environment}-${var.provider-name}"
  image_registry_credential {
    password = var.docker_repo_password
    server   = var.docker_repo_url
    username = var.docker_repo_username
  }
  container {
    cpu    = 2
    image  = "${var.docker_repo_url}/paullatzelsperger/ion-demo/connector:latest"
    memory = "2"
    name   = var.provider-name

    ports {
      port     = 8181
      protocol = "TCP"
    }

    environment_variables = {
      CLIENTID          = azuread_application.demo-app-id.application_id,
      TENANTID          = data.azurerm_client_config.current.tenant_id,
      VAULTNAME         = azurerm_key_vault.main-vault.name,
      CONNECTOR_NAME    = var.provider-name
      TOPIC_NAME        = azurerm_eventgrid_topic.control-topic.name
      TOPIC_ENDPOINT    = azurerm_eventgrid_topic.control-topic.endpoint
      DID_URL           = "did:ion:EiBMres8-U-Gjtfa4CnFUm0URSfMTo1CN4_6Y5J7UeaTyg"
      LOADER_BATCH_SIZE = 2
      DOH_SERVER        = "https://cloudflare-dns.com/dns-query"
    }

    volume {
      mount_path           = "/cert"
      name                 = "certificates"
      share_name           = "certificates"
      storage_account_key  = var.backend_account_key
      storage_account_name = var.backend_account_name
      read_only            = true
    }
  }
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
    password = var.docker_repo_password
    server   = var.docker_repo_url
    username = var.docker_repo_username
  }
  container {
    cpu    = 2
    image  = "${var.docker_repo_url}/paullatzelsperger/ion-demo/connector:latest"
    memory = "2"
    name   = var.consumer-name

    ports {
      port     = 8181
      protocol = "TCP"
    }

    environment_variables = {
      CLIENTID          = azuread_application.demo-app-id.application_id,
      TENANTID          = data.azurerm_client_config.current.tenant_id,
      VAULTNAME         = azurerm_key_vault.main-vault.name,
      CONNECTOR_NAME    = var.consumer-name
      TOPIC_NAME        = azurerm_eventgrid_topic.control-topic.name
      TOPIC_ENDPOINT    = azurerm_eventgrid_topic.control-topic.endpoint
      DID_URL           = "did:ion:EiCmXDhpBoSRyuYTWTTvp1JdyTWpiXJiCnywM6PG87sxAA"
      DOH_SERVER        = "https://cloudflare-dns.com/dns-query"
      LOADER_BATCH_SIZE = 2
    }

    volume {
      mount_path           = "/cert"
      name                 = "certificates"
      share_name           = "certificates"
      storage_account_key  = var.backend_account_key
      storage_account_name = var.backend_account_name
      read_only            = true
    }
  }
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
    password = var.docker_repo_password
    server   = var.docker_repo_url
    username = var.docker_repo_username
  }
  container {
    cpu    = 2
    image  = "${var.docker_repo_url}/paullatzelsperger/ion-demo/connector:latest"
    memory = "2"
    name   = "connector3"

    ports {
      port     = 8181
      protocol = "TCP"
    }

    environment_variables = {
      CLIENTID          = azuread_application.demo-app-id.application_id,
      TENANTID          = data.azurerm_client_config.current.tenant_id,
      VAULTNAME         = azurerm_key_vault.main-vault.name,
      CONNECTOR_NAME    = "connector3"
      TOPIC_NAME        = azurerm_eventgrid_topic.control-topic.name
      TOPIC_ENDPOINT    = azurerm_eventgrid_topic.control-topic.endpoint
      DID_URL           = "did:web:iondemogpstorage.z6.web.core.windows.net"
      DOH_SERVER        = "https://cloudflare-dns.com/dns-query"
      LOADER_BATCH_SIZE = 2
    }

    volume {
      mount_path           = "/cert"
      name                 = "certificates"
      share_name           = "certificates"
      storage_account_key  = var.backend_account_key
      storage_account_name = var.backend_account_name
      read_only            = true
    }
  }
}
