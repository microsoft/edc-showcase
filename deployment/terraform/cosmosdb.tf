resource "azurerm_cosmosdb_account" "asset-index-account" {
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

# Asset Index container for Consumer
resource "azurerm_cosmosdb_sql_container" "consumer-assetindex-container" {
  name                  = var.consumer-name
  resource_group_name   = azurerm_cosmosdb_account.asset-index-account.resource_group_name
  account_name          = azurerm_cosmosdb_account.asset-index-account.name
  database_name         = azurerm_cosmosdb_sql_database.asset-index-db.name
  partition_key_path    = "/partkey"
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
  resource_group_name   = azurerm_cosmosdb_account.asset-index-account.resource_group_name
  account_name          = azurerm_cosmosdb_account.asset-index-account.name
  database_name         = azurerm_cosmosdb_sql_database.asset-index-db.name
  partition_key_path    = "/${var.asset-index-partkey}"
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

# Asset Index container for Connector3
resource "azurerm_cosmosdb_sql_container" "connector3-assetindex-container" {
  name                  = "connector3"
  resource_group_name   = azurerm_cosmosdb_account.asset-index-account.resource_group_name
  account_name          = azurerm_cosmosdb_account.asset-index-account.name
  database_name         = azurerm_cosmosdb_sql_database.asset-index-db.name
  partition_key_path    = "/${var.asset-index-partkey}"
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

# create database that contains all the asset indexes
resource "azurerm_cosmosdb_sql_database" "asset-index-db" {
  name                = "asset-index"
  resource_group_name = azurerm_cosmosdb_account.asset-index-account.resource_group_name
  account_name        = azurerm_cosmosdb_account.asset-index-account.name
  throughput          = 400
}