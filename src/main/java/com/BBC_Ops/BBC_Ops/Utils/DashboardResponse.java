package com.BBC_Ops.BBC_Ops.Utils;

public class DashboardResponse {
    private long totalCustomers;
    private double totalPayments;
    private long pendingPayments;

    public DashboardResponse(long totalCustomers, double totalPayments, long pendingPayments) {
        this.totalCustomers = totalCustomers;
        this.totalPayments = totalPayments;
        this.pendingPayments = pendingPayments;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public double getTotalPayments() {
        return totalPayments;
    }

    public long getPendingPayments() {
        return pendingPayments;
    }
}
