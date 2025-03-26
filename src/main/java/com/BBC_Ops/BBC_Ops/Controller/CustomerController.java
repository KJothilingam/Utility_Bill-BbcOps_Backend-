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
    public ResponseEntity<String> deleteCustomer(@PathVariable Long id) {
        boolean isDeleted = customerService.deleteCustomer(id);
        if (isDeleted) {
            return ResponseEntity.ok("Customer deleted successfully");
        } else {
            return ResponseEntity.status(404).body("Customer not found");
        }
    }


}
