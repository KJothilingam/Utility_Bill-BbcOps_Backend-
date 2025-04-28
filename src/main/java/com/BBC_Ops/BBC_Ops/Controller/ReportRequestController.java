package com.BBC_Ops.BBC_Ops.Controller;

import com.BBC_Ops.BBC_Ops.Enum.RequestStatus;
import com.BBC_Ops.BBC_Ops.Model.ReportRequest;
import com.BBC_Ops.BBC_Ops.Repository.ReportRequestRepository;
import com.BBC_Ops.BBC_Ops.Service.ReportRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@CrossOrigin(origins = "http://localhost:5200", allowCredentials = "true")
public class ReportRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ReportRequestController.class);

    @Autowired
    private ReportRequestService service;

    @Autowired
    private ReportRequestRepository reportRequestRepository;

    @PostMapping
    public ReportRequest submitRequest(@RequestBody ReportRequest request) {
        logger.info("Submitting new report request for customer ID: {}", request.getCustomerId());
        return service.createRequest(request);
    }

    @GetMapping("/customer/{customerId}")
    public List<ReportRequest> getRequestsByCustomerId(@PathVariable Long customerId) {
        logger.info("Fetching report requests for customer ID: {}", customerId);
        return service.getRequestsByCustomerId(customerId);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ReportRequest>> getAllReportRequests() {
        logger.info("Fetching all report requests");
        List<ReportRequest> reports = service.getAllRequests();
        return ResponseEntity.ok(reports);
    }

    @PutMapping("/update-status/{id}")
    public ResponseEntity<Map<String, String>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        logger.info("Updating status for report request ID: {} with new status: {}", id, payload.get("status"));

        String newStatus = payload.get("status");
        Optional<ReportRequest> optionalRequest = reportRequestRepository.findById(id);

        if (optionalRequest.isPresent()) {
            ReportRequest request = optionalRequest.get();
            request.setStatus(RequestStatus.valueOf(newStatus));
            reportRequestRepository.save(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Status updated successfully");
            logger.info("Status updated successfully for report request ID: {}", id);
            return ResponseEntity.ok(response);
        } else {
            logger.error("Report request not found for ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Request not found"));
        }
    }

}
