# vault secrets
resource "azurerm_key_vault_secret" "aws-keyid" {
  name         = "dataspaceconnector-aws-access-key"
  value        = aws_iam_access_key.gx_access_key.id
  key_vault_id = azurerm_key_vault.main-vault.id
  depends_on = [
    azurerm_role_assignment.current-user-secretsofficer
  ]
}

resource "azurerm_key_vault_secret" "aws-secret" {
  name         = "dataspaceconnector-aws-secret-access-key"
  value        = aws_iam_access_key.gx_access_key.secret
  key_vault_id = azurerm_key_vault.main-vault.id
  depends_on = [
    azurerm_role_assignment.current-user-secretsofficer
  ]
}

resource "azurerm_key_vault_secret" "aws-credentials" {
  key_vault_id = azurerm_key_vault.main-vault.id
  name         = "aws-credentials"
  value = jsonencode({
    "accessKeyId"     = aws_iam_access_key.gx_access_key.id,
    "secretAccessKey" = aws_iam_access_key.gx_access_key.secret
  })
  depends_on = [
    azurerm_role_assignment.current-user-secretsofficer
  ]
}

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
  depends_on   = [
    azurerm_role_assignment.current-user-secretsofficer
  ]
}

resource "azurerm_key_vault_secret" "provider_private_key" {
  name         = var.provider-name
  value        = file("../../keys2/provider.pem") # todo: replace with variable
  key_vault_id = azurerm_key_vault.main-vault.id
  depends_on   = [
    azurerm_role_assignment.current-user-secretsofficer
  ]
}

resource "azurerm_key_vault_secret" "verifier_private_key" {
  name         = "verifier"
  value        = file("../../keys2/verifier.pem") # todo: replace with variable
  key_vault_id = azurerm_key_vault.main-vault.id
  depends_on   = [
    azurerm_role_assignment.current-user-secretsofficer
  ]
}
