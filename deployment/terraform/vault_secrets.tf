resource "azurerm_key_vault_secret" "blobstorekey" {
  name         = "${azurerm_storage_account.main-blobstore.name}-key1"
  value        = azurerm_storage_account.main-blobstore.primary_access_key
  key_vault_id = azurerm_key_vault.main-vault.id
  depends_on = [
    azurerm_role_assignment.current-user-secretsofficer
  ]
}

resource "azurerm_key_vault_secret" "consumer_private_key" {
  name         = var.consumer-name
  value        = file("../../keys2/consumer.pem")
  key_vault_id = azurerm_key_vault.main-vault.id
  depends_on = [
    azurerm_role_assignment.current-user-secretsofficer
  ]
}

resource "azurerm_key_vault_secret" "provider_private_key" {
  name         = var.provider-name
  value        = file("../../keys2/provider.pem") # todo: replace with variable
  key_vault_id = azurerm_key_vault.main-vault.id
  depends_on = [
    azurerm_role_assignment.current-user-secretsofficer
  ]
}

resource "azurerm_key_vault_secret" "verifier_private_key" {
  name         = "verifier"
  value        = file("../../keys2/verifier.pem") # todo: replace with variable
  key_vault_id = azurerm_key_vault.main-vault.id
  depends_on = [
    azurerm_role_assignment.current-user-secretsofficer
  ]
}

resource "azurerm_key_vault_secret" "connector3_private_key" {
  name         = "connector3"
  value        = file("../../keys2/connector3.pem") # todo: replace with variable
  key_vault_id = azurerm_key_vault.main-vault.id
  depends_on = [
    azurerm_role_assignment.current-user-secretsofficer
  ]
}

resource "azurerm_key_vault_secret" "cosmos_account_key" {
  key_vault_id = azurerm_key_vault.main-vault.id
  name         = azurerm_cosmosdb_account.showcase-cosmos-account.name
  value        = azurerm_cosmosdb_account.showcase-cosmos-account.primary_key
}