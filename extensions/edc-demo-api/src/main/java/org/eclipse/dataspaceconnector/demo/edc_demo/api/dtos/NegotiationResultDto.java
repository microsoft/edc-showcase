package org.eclipse.dataspaceconnector.demo.edc_demo.api.dtos;

public class NegotiationResultDto {
    private String id;
    private String offerId;


    public NegotiationResultDto() {
    }

    public NegotiationResultDto(String id, String offerId) {
        this.id = id;
        this.offerId = offerId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }
}
