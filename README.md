# IMPORTANT NOTICE: the name of this repository will change soon. While Github will redirect automatically, be sure that you update your git remotes and any URLs that might point to the demo deployment!

# The `edc-showcase` application

Demo Application to show how the EDC can be used to implement distributed identities and federated catalogs.

_This document describes the working concept rather than the finished application._

## Setup

- create 3 private keys and the associated DID document containing the corresponding public key in JWK format. You will
  find a utility script for that purpose in `scripts/did` (use the `-h` option for more details on how to use it).
- pre-define three Hub URLs (ideally they should look exactly how ACI URLs or AKS URLs are generated)
- on every request, generate a JWT signed with the connector private key that you previously generated and containing:
  + the DID URL as claim (payload)
  + an expiration date (t+5min)
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

## Build it

At this time the [Eclipse Dataspace Connector](https://github.com/eclipse-dataspaceconnector/DataSpaceConnector)
repository needs to be built and published to the local maven cache. Assuming you already have that checked out go into
that directory and run

```bash
cd /path/to/eclipse-dataspace-connector-git-repo
./gradlew clean publishToMavenLocal
```

Then, go back into this repository and build it:

```bash
cd /path/to/this/repo
./gradlew clean shadowJar
```

## Deployment

We'll deploy the entire application to Microsoft Azure using Terraform, so that needs to be installed. We'll assume you
have a working understanding of it and won't go into details. All the scripts used in this demo can be found in
the  [deployments folder](deployment/terraform). Also, you'll need to have installed and be logged into Azure CLI and
AWS CLI.

On a command shell type the following to start deployment:

```bash
cd deployment/terraform
terraforn init # only required once
terraform apply
```

## Run it locally

In addition to deploy this demo to an Azure subscription, you can also run it locally, which is helpful if you want to
debug something etc. In order to do that, edit the three `*.properties` files located at `launchers/connector` and
insert your

- client id: that should be printed by the `terraform` command
- tenant id: also, comes from the `terraform` command
- environment: whatever you entered during `terraform` deployment
- DID id: the DID you want to use for your connector

then, on a command line, run:

```bash
java -Dedc.fs.config=launchers/connector/[consumer|provider|connector3].properties -jar launchers/connector/build/libs/connector.jar
```

## Data seeding

- the hubs get their "additional data object" data seeded by
  the [`IdentityHubDataseedingExtension`](extensions/dataseeding/hub/src/main/java/org/eclipse/dataspaceconnector/dataseeding/catalog/IdentityHubDataseedingExtension.java)
  .
- Data objects are claims stored in the Identity Hub. Each data property is a claim, complex properties should just be
  JSON strings

## Interact with the application

Checkout the [Postman collection](resources/MSFT_EDC_Demo.postman_collection.json). You'll need to define variables
for `consumer_url`, `provider_url` and `connector3_url`. Those are used by the request collection.

### Get the catalog

in order to see the data catalog that e.g. the consumer has available, execute

```bash
curl -X GET "http://edc-showcase-consumer.westeurope.azurecontainer.io:8181/api/catalog/cached"
```

that should return an array of Asset objects.

### Start a data transfer

## General notes and restrictions

- The Verifier (or Attestator) in this demo is just another Key Pair
- DIDs are generated and anchored once during initial setup, it does **not** happen during deployment
- The will be one set of object data per hub and one hub per connector (so no filtering at this time)
- The hub runs in its separate runtime and exposes a simple GET API
- connectors **must** have a unique and stable ID
