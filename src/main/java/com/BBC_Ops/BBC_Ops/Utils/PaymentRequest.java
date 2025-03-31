package com.BBC_Ops.BBC_Ops.Utils;

import com.BBC_Ops.BBC_Ops.Enum.PaymentMethod;

public class PaymentRequest {
    private Long billId;
    private Double amount;
    private PaymentMethod paymentMethod;

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
