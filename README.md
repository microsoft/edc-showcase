# The `ion-demo` application

Demo Application to show how the EDC and ION can be used together to implement distributed identities

_This document describes the working concept rather than the finished application._

## Setup

- create 3 Keypairs, one for each connector, one for the Verifier
- pre-define two Hub URLs (ideally they should look exactly how ACI URLs or AKS URLs are generated)
- for each connector:
    + generate a DID Document containing the Public Key and its Hub URL on ION
    + generate a JWT on every request (signed with connectors Private Key) containing the DID URL as claim (=payload)
      and an expiration date (t+5min)
- for the Verifier (="accenture"): put a DID with it's public key on ION
- create a certificate and a private key in `*.pem` format as well as the corresponding `*.pfx` file:
    - generate the files:
       ```bash 
       openssl req -newkey rsa:2048 -new -nodes -x509 -days 3650 -keyout key.pem -out cert.pem
       openssl pkcs12 -inkey key.pem -in cert.cert -export -out cert.pfx
       ```
    - store the contents of `cert.pfx` in an environment variable named `TF_VAR_CERTIFICATE` (assuming `bash` syntax):
      ```bash
      export TF_VAR_CERTIFICATE=$(<PATH/TO/cert/cert.pem) # the "<" is important!
      # to verify:
      echo $TF_VAR_CERTIFICATE # should print the pem-encoded certificate
      ```

## Deployment

We'll deploy the entire application to Microsoft Azure using Terraform, so that needs to be installed. We'll assume you
have a working understanding of it and won't go into details. All the scripts used in this demo can be found in
the  [deployments folder](deployment/terraform). Also, you'll need the Azure CLI installed and be logged.

On a command shell type the following to start deployment:

```bash
cd deployment/terraform
terraform apply
```

## Data seeding

- the hubs get their "additional data object" data seeded by
  the [`IdentityHubDataseedingExtension`](extensions/dataseeding/hub/src/main/java/org/eclipse/dataspaceconnector/dataseeding/catalog/IdentityHubDataseedingExtension.java)
  .
- additional data objects are again JWEs signed with the Verifier's private key. Each data property is a claim, complex
  properties should just be JSON strings

## Interact with the application

Currently, the only way to interact with the application is via REST. Please take a look at
the [controller](extensions/public-rest-api/src/main/java/org/eclipse/dataspaceconnector/demo/api/rest/IonDemoApiController.java) (
no Swagger yet). in order to verify that the application is running as intended, type

```bash
curl -X GET "http://ion-demo-consumer.westeurope.azurecontainer.io:8181/api/catalog?connectorAddress=http://ion-demo-provider.westeurope.azurecontainer.io:8181"
```

and if that returns `["test-document"]` after a second or two, your fine.

## General notes and restrictions

- The Verifier (or Attestator) in this demo is just another Key Pair
- DIDs are generated and anchored once during initial setup, it does **not** happen during deployment
- The will be one set of object data per hub and one hub per connector (so no filtering at this time)
- The hub runs in its separate runtime and exposes a simple GET API
- connectors **must** have a unique and stable ID
