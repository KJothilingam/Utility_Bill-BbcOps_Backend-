package com.BBC_Ops.BBC_Ops.Controller;

import com.BBC_Ops.BBC_Ops.Model.ApiResponse;
import com.BBC_Ops.BBC_Ops.Model.Customer;
import com.BBC_Ops.BBC_Ops.Service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/list")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadCsvFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "File is empty", 0, 0));
        }

        try {
            Map<String, Object> response = customerService.processCsv(file);
            boolean success = (boolean) response.getOrDefault("success", false);
            int validRecords = (int) response.getOrDefault("validRecords", 0);
            int rejectedRecords = (int) response.getOrDefault("rejectedRecords", 0);
            List<String> errors = (List<String>) response.getOrDefault("errors", new ArrayList<>());

            if (success) {
                return ResponseEntity.ok(new ApiResponse(true, "File uploaded successfully", validRecords, rejectedRecords));
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Validation errors", validRecords, rejectedRecords, errors));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Server error: " + e.getMessage(), 0, 0));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable Long id) {
        Map<String, Object> response = customerService.deleteCustomer(id);

        if ((boolean) response.get("status")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(404).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCustomer(
            @PathVariable Long id, @RequestBody Customer updatedCustomer) {

        Customer savedCustomer = customerService.updateCustomer(id, updatedCustomer);
        Map<String, Object> response = new HashMap<>();

        if (savedCustomer != null) {
            response.put("status", true);
            response.put("customer", Map.of(
                    "customerId", savedCustomer.getCustomerId(),
                    "name", savedCustomer.getName(),
                    "email", savedCustomer.getEmail(),
                    "phoneNumber", savedCustomer.getPhoneNumber(),
                    "address", savedCustomer.getAddress(),
                    "unitConsumption", savedCustomer.getUnitConsumption(),
                    "billDueDate", savedCustomer.getBillDueDate().toString(), // Convert to String
                    "meterNumber", savedCustomer.getMeterNumber(),
                    "connectionType", savedCustomer.getConnectionType()
            ));
        } else {
            response.put("status", false);
            response.put("customer", null);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public Map<String, Object> addCustomer(@RequestBody Customer customer) {
        return customerService.addCustomer(customer);
    }

}
