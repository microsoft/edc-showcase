FROM openjdk:11-jre-slim

WORKDIR /app
COPY ./build/libs/connector.jar /app

EXPOSE 8181

ENTRYPOINT java \
    -Dedc.vault.clientid=${CLIENTID} \
    -Dedc.vault.tenantid=${TENANTID} \
    -Dedc.vault.certificate=/cert/cert.pfx \
    -Dedc.vault.name=${VAULTNAME} \
    -Ddataspaceconnector.connector.name=${CONNECTOR_NAME} \
    -Dedc.events.topic.name=${TOPIC_NAME} \
    -Dedc.events.topic.endpoint=${TOPIC_ENDPOINT} \
    -Dedc.identity.did.url=${DID_URL} \
    -Djava.security.edg=file:/dev/.urandom -jar connector.jar