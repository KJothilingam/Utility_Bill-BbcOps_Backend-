package com.BBC_Ops.BBC_Ops.Utils;


public class PaymentSummaryDTO {
    private long pending;
    private long paid;
    private long overdue;

    public PaymentSummaryDTO(long pending, long paid, long overdue) {
        this.pending = pending;
        this.paid = paid;
        this.overdue = overdue;
    }

    public long getPending() { return pending; }
    public long getPaid() { return paid; }
    public long getOverdue() { return overdue; }
}