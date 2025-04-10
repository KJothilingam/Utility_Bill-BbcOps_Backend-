package com.BBC_Ops.BBC_Ops.Model;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name = "employee_audit_logs")
public class EmployeeAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;
    private String name;
    private String designation;

    private String actionMessage; // e.g., "Logged In", "Logged Out", "Updated profile"

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date();

    @Override
    public String toString() {
        return "EmployeeAuditLog{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", name='" + name + '\'' +
                ", designation='" + designation + '\'' +
                ", actionMessage='" + actionMessage + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getActionMessage() {
        return actionMessage;
    }

    public void setActionMessage(String actionMessage) {
        this.actionMessage = actionMessage;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
