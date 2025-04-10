package com.BBC_Ops.BBC_Ops.Controller;

import com.BBC_Ops.BBC_Ops.Enum.RequestStatus;
import com.BBC_Ops.BBC_Ops.Model.ReportRequest;
import com.BBC_Ops.BBC_Ops.Repository.ReportRequestRepository;
import com.BBC_Ops.BBC_Ops.Service.ReportRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("report")

public class ReportRequestController {

    @Autowired
    private ReportRequestService service;

    @PostMapping
    public ReportRequest submitRequest(@RequestBody ReportRequest request) {
        return service.createRequest(request);
    }

    @GetMapping("/customer/{customerId}")
    public List<ReportRequest> getRequestsByCustomerId(@PathVariable Long customerId) {
        return service.getRequestsByCustomerId(customerId);
    }
    @GetMapping("/all")
    public ResponseEntity<List<ReportRequest>> getAllReportRequests() {
        List<ReportRequest> reports = service.getAllRequests();
        return ResponseEntity.ok(reports);
    }

    @Autowired
    private ReportRequestRepository reportRequestRepository;


    @PutMapping("/update-status/{id}")
    public ResponseEntity<Map<String, String>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newStatus = payload.get("status");
        Optional<ReportRequest> optionalRequest = reportRequestRepository.findById(id);

        if (optionalRequest.isPresent()) {
            ReportRequest request = optionalRequest.get();
            request.setStatus(RequestStatus.valueOf(newStatus));
            reportRequestRepository.save(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Status updated successfully");

            return ResponseEntity.ok(response); //  return JSON map
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Request not found"));
        }
    }


}
