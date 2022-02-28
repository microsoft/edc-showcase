# -buster is required to have apt available
FROM openjdk:11-jre-slim-buster

# by default curl is not available, so install it
RUN apt update && apt install curl -y

WORKDIR /app
COPY ./build/libs/connector.jar /app

EXPOSE 8181

# health status is determined by the availability of the /health endpoint
#HEALTHCHECK --interval=5s --timeout=5s --retries=10 CMD curl --fail -X GET http://localhost:8181/api/health || exit 1

ENTRYPOINT java \
    -Dids.webhook.address=${IDS_WEBHOOK_ADDRESS} \
    -Dedc.api.control.auth.apikey.value=x-edc-showcase-x \
    -Dedc.web.rest.cors.enabled=true \
    -Dedc.vault.clientid=${CLIENTID} \
    -Dedc.vault.tenantid=${TENANTID} \
    -Dedc.vault.certificate=/cert/cert.pfx \
    -Dedc.vault.name=${VAULTNAME} \
    -Dedc.connector.name=${CONNECTOR_NAME} \
    -Dedc.events.topic.name=${TOPIC_NAME} \
    -Dedc.events.topic.endpoint=${TOPIC_ENDPOINT} \
    -Dedc.identity.did.url=${DID_URL} \
    -Dedc.catalog.cache.loader.batch-size=${LOADER_BATCH_SIZE} \
    -Dedc.webdid.doh.url=${DOH_SERVER} \
    -Dedc.assetindex.cosmos.account-name=${COSMOS_ACCOUNT}  \
    -Dedc.assetindex.cosmos.database-name=${COSMOS_DB} \
    -Dedc.cosmos.partition-key=${COSMOS_PARTKEY} \
    -Dedc.assetindex.cosmos.container-name=${COSMOS_CONTAINER} \

    -Dedc.contractdefinitionstore.cosmos.account-name=${COSMOS_ACCOUNT} \
    -Dedc.contractdefinitionstore.cosmos.database-name=${CDS_DATABASE} \
    -Dedc.contractdefinitionstore.cosmos.container-name=${CDS_CONTAINER} \

    -Dedc.contractnegotiationstore.cosmos.account-name=${COSMOS_ACCOUNT} \
    -Dedc.contractnegotiationstore.cosmos.database-name=${CNS_DATABASE} \
    -Dedc.contractnegotiationstore.cosmos.container-name=${CNS_CONTAINER} \

    -Djava.security.edg=file:/dev/.urandom -jar connector.jar
