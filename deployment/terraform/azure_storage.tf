#storage account
resource "azurerm_storage_account" "main-blobstore" {
  name                     = "${replace(var.environment, "-", "")}gpstorage"
  resource_group_name      = azurerm_resource_group.core-resourcegroup.name
  location                 = azurerm_resource_group.core-resourcegroup.location
  account_tier             = "Standard"
  account_replication_type = "GRS"
  account_kind             = "StorageV2"
  //allows for blobs, queues, fileshares, etc.
  static_website {
    index_document = "index.html"
  }
}

# storage container
resource "azurerm_storage_container" "main-blob-container" {

  name                 = "src-container"
  storage_account_name = azurerm_storage_account.main-blobstore.name
}

# put a file as blob to the storage container
resource "azurerm_storage_blob" "testfile" {
  name                   = "test-document.txt"
  storage_account_name   = azurerm_storage_account.main-blobstore.name
  storage_container_name = azurerm_storage_container.main-blob-container.name
  type                   = "Block"
  source                 = "test-document.txt"
}

resource "azurerm_storage_blob" "testfile3" {
  name                   = "complex_schematic_drawing.jpg"
  storage_account_name   = azurerm_storage_account.main-blobstore.name
  storage_container_name = azurerm_storage_container.main-blob-container.name
  type                   = "Block"
  source                 = "complex_schematic_drawing.jpg"
}

# the index file for static web content, i.e. the Web DID
resource "azurerm_storage_blob" "index-html" {
  name                   = "index.html"
  storage_account_name   = azurerm_storage_account.main-blobstore.name
  storage_container_name = "$web"
  type                   = "Block"
  source                 = "../did-web/index.html"
  content_type           = "text/html"
}

# upload the DID document for the consumer
resource "azurerm_storage_blob" "consumer-webdid" {
  name                   = "consumer/did.json"
  storage_account_name   = azurerm_storage_account.main-blobstore.name
  storage_container_name = "$web"
  type                   = "Block"
  source                 = "../did-web/consumer.json"
  content_type           = "applicaton/json"
}

# upload the DID document for the provider
resource "azurerm_storage_blob" "provider-webdid" {
  name                   = "provider/did.json"
  storage_account_name   = azurerm_storage_account.main-blobstore.name
  storage_container_name = "$web"
  type                   = "Block"
  source                 = "../did-web/provider.json"
  content_type           = "applicaton/json"
}

# upload the DID document for the 3rd connector
resource "azurerm_storage_blob" "connector3-webdid" {
  name                   = "connector3/did.json"
  storage_account_name   = azurerm_storage_account.main-blobstore.name
  storage_container_name = "$web"
  type                   = "Block"
  source                 = "../did-web/connector3.json"
  content_type           = "applicaton/json"
}