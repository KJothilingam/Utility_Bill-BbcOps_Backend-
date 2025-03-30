package com.BBC_Ops.BBC_Ops.Utils;

import com.BBC_Ops.BBC_Ops.Model.Bill;

public class BillResponse {
    private boolean success;
    private String message;
    private Bill bill;

    public BillResponse(boolean success, String message, Bill bill) {
        this.success = success;
        this.message = message;
        this.bill = bill;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }
}
