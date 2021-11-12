FROM openjdk:11-jre-slim

WORKDIR /app
COPY ./build/libs/connector.jar /app

EXPOSE 8181

ENTRYPOINT java \
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
    -Djava.security.edg=file:/dev/.urandom -jar connector.jar
