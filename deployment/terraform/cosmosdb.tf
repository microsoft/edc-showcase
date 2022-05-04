resource "azurerm_cosmosdb_account" "showcase-cosmos-account" {
  name                = "${var.environment}-cosmos"
  location            = azurerm_resource_group.core-resourcegroup.location
  resource_group_name = azurerm_resource_group.core-resourcegroup.name
  offer_type          = "Standard"
  kind                = "GlobalDocumentDB"

  enable_automatic_failover = false
  #  enable_free_tier          = true

  capabilities {
    name = "EnableAggregationPipeline"
  }

  consistency_policy {
    consistency_level = "Strong"
  }

  geo_location {
    location          = azurerm_resource_group.core-resourcegroup.location
    failover_priority = 0
  }
}

# create database that contains all the asset indexes
resource "azurerm_cosmosdb_sql_database" "asset-index-db" {
  name                = "asset-index"
  resource_group_name = azurerm_cosmosdb_account.showcase-cosmos-account.resource_group_name
  account_name        = azurerm_cosmosdb_account.showcase-cosmos-account.name
  throughput          = 400
}

# Asset Index container for Consumer
resource "azurerm_cosmosdb_sql_container" "consumer-assetindex-container" {
  name                  = var.consumer-name
  resource_group_name   = azurerm_cosmosdb_account.showcase-cosmos-account.resource_group_name
  account_name          = azurerm_cosmosdb_account.showcase-cosmos-account.name
  database_name         = azurerm_cosmosdb_sql_database.asset-index-db.name
  partition_key_path    = "/partitionKey"
  partition_key_version = 1
  throughput            = 400

  indexing_policy {
    indexing_mode = "Consistent"

    included_path {
      path = "/*"
    }

    included_path {
      path = "/included/?"
    }

    excluded_path {
      path = "/excluded/?"
    }
  }

}

# Asset Index container for Provider
resource "azurerm_cosmosdb_sql_container" "provider-assetindex-container" {
  name                  = var.provider-name
  resource_group_name   = azurerm_cosmosdb_account.showcase-cosmos-account.resource_group_name
  account_name          = azurerm_cosmosdb_account.showcase-cosmos-account.name
  database_name         = azurerm_cosmosdb_sql_database.asset-index-db.name
  partition_key_path    = "/partitionKey"
  partition_key_version = 1
  throughput            = 400

  indexing_policy {
    indexing_mode = "Consistent"

    included_path {
      path = "/*"
    }

    included_path {
      path = "/included/?"
    }

    excluded_path {
      path = "/excluded/?"
    }
  }
}


# create database that contains all the contract-definition indexes
resource "azurerm_cosmosdb_sql_database" "contractdefinition-store-db" {
  name                = "contract-definition-store"
  resource_group_name = azurerm_cosmosdb_account.showcase-cosmos-account.resource_group_name
  account_name        = azurerm_cosmosdb_account.showcase-cosmos-account.name
  throughput          = 400
}

# ContractDefinition-Store container for Consumer
resource "azurerm_cosmosdb_sql_container" "consumer-contractdefstore-container" {
  name                  = var.consumer-name
  resource_group_name   = azurerm_cosmosdb_account.showcase-cosmos-account.resource_group_name
  account_name          = azurerm_cosmosdb_account.showcase-cosmos-account.name
  database_name         = azurerm_cosmosdb_sql_database.contractdefinition-store-db.name
  partition_key_path    = "/partitionKey"
  partition_key_version = 1
  throughput            = 400

  indexing_policy {
    indexing_mode = "Consistent"

    included_path {
      path = "/*"
    }

    included_path {
      path = "/included/?"
    }

    excluded_path {
      path = "/excluded/?"
    }
  }

}

# ContractDefinition-Store container for Provider
resource "azurerm_cosmosdb_sql_container" "provider-contractdefstore-container" {
  name                  = var.provider-name
  resource_group_name   = azurerm_cosmosdb_account.showcase-cosmos-account.resource_group_name
  account_name          = azurerm_cosmosdb_account.showcase-cosmos-account.name
  database_name         = azurerm_cosmosdb_sql_database.contractdefinition-store-db.name
  partition_key_path    = "/partitionKey"
  partition_key_version = 1
  throughput            = 400

  indexing_policy {
    indexing_mode = "Consistent"

    included_path {
      path = "/*"
    }

    included_path {
      path = "/included/?"
    }

    excluded_path {
      path = "/excluded/?"
    }
  }
}


# create database that contains all the contract-definition indexes
resource "azurerm_cosmosdb_sql_database" "contractnegotiation-store-db" {
  name                = "contract-negotiation-store"
  resource_group_name = azurerm_cosmosdb_account.showcase-cosmos-account.resource_group_name
  account_name        = azurerm_cosmosdb_account.showcase-cosmos-account.name
  throughput          = 400
}

# ContractDefinition-Store container for Consumer
resource "azurerm_cosmosdb_sql_container" "consumer-contractnegotiation-container" {
  name                  = var.consumer-name
  resource_group_name   = azurerm_cosmosdb_account.showcase-cosmos-account.resource_group_name
  account_name          = azurerm_cosmosdb_account.showcase-cosmos-account.name
  database_name         = azurerm_cosmosdb_sql_database.contractnegotiation-store-db.name
  partition_key_path    = "/partitionKey"
  partition_key_version = 1
  throughput            = 400

  indexing_policy {
    indexing_mode = "Consistent"

    included_path {
      path = "/*"
    }

    included_path {
      path = "/included/?"
    }

    excluded_path {
      path = "/excluded/?"
    }
  }

}

# ContractDefinition-Store container for Provider
resource "azurerm_cosmosdb_sql_container" "provider-contractnegotiation-container" {
  name                  = var.provider-name
  resource_group_name   = azurerm_cosmosdb_account.showcase-cosmos-account.resource_group_name
  account_name          = azurerm_cosmosdb_account.showcase-cosmos-account.name
  database_name         = azurerm_cosmosdb_sql_database.contractnegotiation-store-db.name
  partition_key_path    = "/partitionKey"
  partition_key_version = 1
  throughput            = 400

  indexing_policy {
    indexing_mode = "Consistent"

    included_path {
      path = "/*"
    }

    included_path {
      path = "/included/?"
    }

    excluded_path {
      path = "/excluded/?"
    }
  }
}


# Stored Procedures for Contract Negotiation Store
resource "azurerm_cosmosdb_sql_stored_procedure" "nextForState-consumer" {
  name                = "nextForState"
  resource_group_name = azurerm_cosmosdb_account.showcase-cosmos-account.resource_group_name
  account_name        = azurerm_cosmosdb_account.showcase-cosmos-account.name
  database_name       = azurerm_cosmosdb_sql_database.contractnegotiation-store-db.name
  container_name      = azurerm_cosmosdb_sql_container.consumer-contractnegotiation-container.name
  body                = file("nextForState.js")
}

resource "azurerm_cosmosdb_sql_stored_procedure" "nextForState-provider" {
  name                = "nextForState"
  resource_group_name = azurerm_cosmosdb_account.showcase-cosmos-account.resource_group_name
  account_name        = azurerm_cosmosdb_account.showcase-cosmos-account.name
  database_name       = azurerm_cosmosdb_sql_database.contractnegotiation-store-db.name
  container_name      = azurerm_cosmosdb_sql_container.provider-contractnegotiation-container.name

  body = file("nextForState.js")
}
resource "azurerm_cosmosdb_sql_stored_procedure" "lease-consumer" {
  name                = "lease"
  resource_group_name = azurerm_cosmosdb_account.showcase-cosmos-account.resource_group_name
  account_name        = azurerm_cosmosdb_account.showcase-cosmos-account.name
  database_name       = azurerm_cosmosdb_sql_database.contractnegotiation-store-db.name
  container_name      = azurerm_cosmosdb_sql_container.consumer-contractnegotiation-container.name
  body                = file("lease.js")
}

resource "azurerm_cosmosdb_sql_stored_procedure" "lease-provider" {
  name                = "lease"
  resource_group_name = azurerm_cosmosdb_account.showcase-cosmos-account.resource_group_name
  account_name        = azurerm_cosmosdb_account.showcase-cosmos-account.name
  database_name       = azurerm_cosmosdb_sql_database.contractnegotiation-store-db.name
  container_name      = azurerm_cosmosdb_sql_container.provider-contractnegotiation-container.name

  body = file("lease.js")
}