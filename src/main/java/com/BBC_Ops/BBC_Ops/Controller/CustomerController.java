package com.BBC_Ops.BBC_Ops.Controller;

import com.BBC_Ops.BBC_Ops.Model.ApiResponse;
import com.BBC_Ops.BBC_Ops.Model.Customer;
import com.BBC_Ops.BBC_Ops.Model.Employee;
import com.BBC_Ops.BBC_Ops.Repository.CustomerRepository;
import com.BBC_Ops.BBC_Ops.Service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

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

            ApiResponse apiResponse = new ApiResponse(success, "File processed", validRecords, rejectedRecords, errors);

            if (validRecords > 0 && rejectedRecords > 0) {
                // ✅ Partial success (Return 200 instead of 400)
                return ResponseEntity.ok(apiResponse);
            } else if (success) {
                // ✅ Full success
                return ResponseEntity.ok(apiResponse);
            } else {
                // ❌ Complete failure (return 400)
                return ResponseEntity.badRequest().body(apiResponse);
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

        Map<String, Object> response = new HashMap<>();
        Customer savedCustomer = customerService.updateCustomer(id, updatedCustomer);

        if (savedCustomer != null) {
            response.put("status", true);
            response.put("customer", Map.of(
                    "customerId", savedCustomer.getCustomerId(),
                    "name", savedCustomer.getName(),
                    "email", savedCustomer.getEmail(),
                    "phoneNumber", savedCustomer.getPhoneNumber(),
                    "address", savedCustomer.getAddress(),
                    "unitConsumption", savedCustomer.getUnitConsumption(),
                    "billDueDate", savedCustomer.getBillDueDate().toString(),
                    "meterNumber", savedCustomer.getMeterNumber(),
                    "connectionType", savedCustomer.getConnectionType()
            ));
        } else {
            response.put("status", false);
            response.put("message", "Customer not found or update failed.");
        }

        return ResponseEntity.ok(response);
    }
    private Map<String, String> otpStorage = new HashMap<>();

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addCustomer(@RequestBody Customer customer) {
        Map<String, Object> response = customerService.addCustomer(customer);

        if ((boolean) response.get("success")) {
            return ResponseEntity.ok(response); // Return success response
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // Return error response
        }
    }
    @PostMapping("/generate-otp")
    public ResponseEntity<?> generateOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        // Check if email exists in DB
        Customer customer = customerService.findByEmail(email);
        if (customer == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email not registered"));
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Store OTP temporarily (for demo purposes, use Redis in production)
        otpStorage.put(email, otp);

        // ✅ Print OTP in server logs
        System.out.println("Generated OTP for " + email + ": " + otp);

        return ResponseEntity.ok(Map.of("message", "OTP sent successfully", "otp", otp));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String enteredOtp = request.get("otp");

        // Validate OTP
        if (otpStorage.containsKey(email) && otpStorage.get(email).equals(enteredOtp)) {
            otpStorage.remove(email); // Clear OTP after successful verification

            // Fetch customer details
            Customer customer = customerService.findByEmail(email);
            if (customer == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }

            return ResponseEntity.ok(Map.of(
                    "message", "OTP verified successfully",
                    "customerId", customer.getCustomerId(),
                    "customerName", customer.getName() // Assuming getName() returns full name
            ));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP"));
    }



    @Autowired
    private CustomerRepository customerRepository;
    // ✅ Fetch customer by ID
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }




}
