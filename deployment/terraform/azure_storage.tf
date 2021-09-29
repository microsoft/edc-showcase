#storage account
resource "azurerm_storage_account" "main-blobstore" {
  name                     = "${replace(var.environment, "-", "")}gpstorage"
  resource_group_name      = azurerm_resource_group.core-resourcegroup.name
  location                 = azurerm_resource_group.core-resourcegroup.location
  account_tier             = "Standard"
  account_replication_type = "GRS"
  account_kind             = "StorageV2"
  //allows for blobs, queues, fileshares, etc.
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