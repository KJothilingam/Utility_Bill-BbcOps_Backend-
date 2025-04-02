package com.BBC_Ops.BBC_Ops.Controller;

import com.BBC_Ops.BBC_Ops.Model.PaymentRecord;
import com.BBC_Ops.BBC_Ops.Service.DashboardService;
import com.BBC_Ops.BBC_Ops.Service.PaymentService;
import com.BBC_Ops.BBC_Ops.Utils.DashboardResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboardData() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/latest-payments")
    public ResponseEntity<List<PaymentRecord>> getLatestPayments() {
        return ResponseEntity.ok(dashboardService.getLatestPayments());
    }

    @Autowired
    private PaymentService paymentService;
    @GetMapping("/weekly-payments")
    public Map<String, Object> getWeeklyPayments() {
        return paymentService.getWeeklyPayments();
    }

}
