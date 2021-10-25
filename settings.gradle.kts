rootProject.name = "ion-demo"

include(":launchers:connector")
//disabled temporarily due to compile errors
//include(":launchers:junit")
include(":launchers:registration-service")

include(":extensions:dataseeding:hub")
include(":extensions:dataseeding:catalog")
include(":extensions:public-rest-api")
include(":extensions:transfer-azure-s3")
include(":extensions:identity-hub-verifier")
