package org.eclipse.dataspaceconnector.demo.edc_demo.api.dtos;

public class TransferProcessCreationDto {

    private String assetId;
    private String contractId;
    private String dataDestinationType;
    private String connectorAddress;

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public String getDataDestinationType() {
        return dataDestinationType;
    }

    public void setDataDestinationType(String dataDestination) {
        this.dataDestinationType = dataDestination;
    }

    public String getConnectorAddress() {
        return connectorAddress;
    }

    public void setConnectorAddress(String connectorAddress) {
        this.connectorAddress = connectorAddress;
    }
}
