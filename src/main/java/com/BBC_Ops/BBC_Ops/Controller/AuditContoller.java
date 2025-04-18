package com.BBC_Ops.BBC_Ops.Controller;


import com.BBC_Ops.BBC_Ops.Model.Employee;
import com.BBC_Ops.BBC_Ops.Model.EmployeeAuditLog;
import com.BBC_Ops.BBC_Ops.Service.AuditService;
import com.BBC_Ops.BBC_Ops.Utils.AuditLogRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("audit")
public class AuditContoller {

    @Autowired
    private AuditService auditService;

    @GetMapping("/audit-logs")
    public List<EmployeeAuditLog> getAllLogs() {
        return auditService.getAllLogs();
    }

    @PostMapping("/log")
    public void log(@RequestBody AuditLogRequest request) {
        auditService.logAction(request.getEmployee(), request.getMessage());
    }
}
