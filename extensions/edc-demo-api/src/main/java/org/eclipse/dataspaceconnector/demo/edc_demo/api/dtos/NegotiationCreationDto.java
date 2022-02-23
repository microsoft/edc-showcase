package org.eclipse.dataspaceconnector.demo.edc_demo.api.dtos;

public class NegotiationCreationDto {
    private String protocol = "ids-multipart";
    private String connectorAddress;
    private String connectorId;
    private String offerId;

    public NegotiationCreationDto() {
    }

    public NegotiationCreationDto(String connectorAddress, String connectorId, String offerId) {
        this.connectorAddress = connectorAddress;
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

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }
}
