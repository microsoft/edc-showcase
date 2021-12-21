package org.eclipse.dataspaceconnector.demo.api.rest;

public class NegotiationDto {
    private String protocol = "ids-multipart";
    private String connectorAddress;
    private String connectorId;
    private String offerId;

    public NegotiationDto() {
    }

    public NegotiationDto(String connectorAddress, String connectorId, String offerId) {
        this.connectorAddress = connectorAddress;
        this.connectorId = connectorId;
        this.offerId = offerId;
    }

    public String getConnectorAddress() {
        return connectorAddress;
    }

    public void setConnectorAddress(String connectorAddress) {
        this.connectorAddress = connectorAddress;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }
}
