package com.BBC_Ops.BBC_Ops.Service;

import com.BBC_Ops.BBC_Ops.Model.Employee;
import com.BBC_Ops.BBC_Ops.Model.EmployeeAuditLog;
import com.BBC_Ops.BBC_Ops.Repository.EmployeeAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuditService {

    @Autowired
    private EmployeeAuditLogRepository auditLogRepository;

    public void logAction(Employee employee, String actionMessage) {
        EmployeeAuditLog log = new EmployeeAuditLog();
        log.setEmployeeId(employee.getEmployeeId());
        log.setName(employee.getName());
        log.setDesignation(employee.getDesignation());
        log.setActionMessage(actionMessage);
        log.setTimestamp(new Date());

        auditLogRepository.save(log);
    }
}
