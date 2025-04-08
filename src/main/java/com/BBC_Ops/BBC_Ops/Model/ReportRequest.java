package com.BBC_Ops.BBC_Ops.Model;

import com.BBC_Ops.BBC_Ops.Enum.ReportRequestType;
import com.BBC_Ops.BBC_Ops.Enum.RequestStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
@Entity
public class ReportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    @Enumerated(EnumType.STRING)
    private ReportRequestType requestType;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private String details;

    private Long billId;

    private Long customerId;

    private LocalDate requestDate;

    private String newValue;
    private Integer extendDays;

    @PrePersist
    public void onCreate() {
        this.requestDate = LocalDate.now();
    }

    // Getters and Setters (for new fields too)


    @Override
    public String toString() {
        return "ReportRequest{" +
                "requestId=" + requestId +
                ", requestType=" + requestType +
                ", status=" + status +
                ", details='" + details + '\'' +
                ", billId=" + billId +
                ", customerId=" + customerId +
                ", requestDate=" + requestDate +
                ", newValue='" + newValue + '\'' +
                ", extendDays=" + extendDays +
                '}';
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public ReportRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(ReportRequestType requestType) {
        this.requestType = requestType;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Integer getExtendDays() {
        return extendDays;
    }

    public void setExtendDays(Integer extendDays) {
        this.extendDays = extendDays;
    }
}
