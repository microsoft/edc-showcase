package org.eclipse.dataspaceconnector.demo.edc_demo.api.dtos;

import org.eclipse.dataspaceconnector.spi.types.domain.transfer.*;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

public class TransferProcessDto {
    private String id;
    private String type;
    private int state;
    private Timestamp stateTimestamp;
    private String errorDetail;
    private String connectorAddress;
    private String protocol;
    private String connectorId;
    private String assetId;
    private String contractId;
    private String dataDestinationType;
    private String destinationAddress;

    public TransferProcessDto(String id, String type, int state, Timestamp stateTimestamp, String errorDetail, String connectorAddress, String protocol, String connectorId, String assetId, String contractId, String dataDestinationType, String destinationAddress) {
        this.id = id;
        this.type = type;
        this.state = state;
        this.stateTimestamp = stateTimestamp;
        this.errorDetail = errorDetail;
        this.connectorAddress = connectorAddress;
        this.protocol = protocol;
        this.connectorId = connectorId;
        this.assetId = assetId;
        this.contractId = contractId;
        this.dataDestinationType = dataDestinationType;
        this.destinationAddress = destinationAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Timestamp getStateTimestamp() {
        return stateTimestamp;
    }

    public void setStateTimestamp(Timestamp stateTimestamp) {
        this.stateTimestamp = stateTimestamp;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void setErrorDetail(String errorDetail) {
        this.errorDetail = errorDetail;
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

    public void setDataDestinationType(String dataDestinationType) {
        this.dataDestinationType = dataDestinationType;
    }

    public String getDestinationAddress() { return destinationAddress; }

    public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
}
