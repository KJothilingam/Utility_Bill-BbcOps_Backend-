package com.BBC_Ops.BBC_Ops.Model;

import java.util.List;

public class ApiResponse {
    private boolean success;
    private String message;
    private int validRecords;
    private int rejectedRecords;
    private List<String> errors; // List of validation errors

    // Constructor for success response without errors
    public ApiResponse(boolean success, String message, int validRecords, int rejectedRecords) {
        this.success = success;
        this.message = message;
        this.validRecords = validRecords;
        this.rejectedRecords = rejectedRecords;
        this.errors = null;
    }

    // Constructor for validation errors
    public ApiResponse(boolean success, String message, int validRecords, int rejectedRecords, List<String> errors) {
        this.success = success;
        this.message = message;
        this.validRecords = validRecords;
        this.rejectedRecords = rejectedRecords;
        this.errors = errors;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getValidRecords() {
        return validRecords;
    }

    public int getRejectedRecords() {
        return rejectedRecords;
    }

    public List<String> getErrors() {
        return errors;
    }
}
