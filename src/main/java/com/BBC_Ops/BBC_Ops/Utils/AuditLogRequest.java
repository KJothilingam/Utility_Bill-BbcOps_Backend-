package com.BBC_Ops.BBC_Ops.Utils;


import com.BBC_Ops.BBC_Ops.Model.Employee;

public class AuditLogRequest {
    private Employee employee;
    private String message;

    // Getters and Setters
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
