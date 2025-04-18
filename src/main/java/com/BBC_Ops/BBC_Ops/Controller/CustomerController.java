package com.BBC_Ops.BBC_Ops.Controller;

import com.BBC_Ops.BBC_Ops.Model.ActiveToken;
import com.BBC_Ops.BBC_Ops.Model.ApiResponse;
import com.BBC_Ops.BBC_Ops.Model.Customer;
import com.BBC_Ops.BBC_Ops.Repository.ActiveTokenRepository;
import com.BBC_Ops.BBC_Ops.Repository.CustomerRepository;
import com.BBC_Ops.BBC_Ops.Service.CustomerService;
import com.BBC_Ops.BBC_Ops.configuration.CustomerJwtUtil;
import com.BBC_Ops.BBC_Ops.configuration.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerJwtUtil customerJwtUtil;

    @Autowired
    private ActiveTokenRepository activeTokenRepository;


    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private Map<String, String> otpStorage = new HashMap<>();
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
                //  Partial success (Return 200 instead of 400)
                return ResponseEntity.ok(apiResponse);
            } else if (success) {
                //  Full success
                return ResponseEntity.ok(apiResponse);
            } else {
                //  Complete failure (return 400)
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
//                    "billDueDate", savedCustomer.getBillDueDate().toString(),
                    "meterNumber", savedCustomer.getMeterNumber(),
                    "connectionType", savedCustomer.getConnectionType()
            ));
        } else {
            response.put("status", false);
            response.put("message", "Customer not found or update failed.");
        }

        return ResponseEntity.ok(response);
    }


    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addCustomer(@RequestBody Customer customer) {
        Map<String, Object> response = customerService.addCustomer(customer);

        if ((boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/generate-otp")
    public ResponseEntity<?> generateOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("OTP generation requested for email: {}", email);

        Customer customer = customerService.findByEmail(email);
        if (customer == null) {
            logger.warn("OTP generation failed - email not registered: {}", email);
            return ResponseEntity.badRequest().body(Map.of("message", "Email not registered"));
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);
        logger.info("Generated OTP for {}: {}", email, otp);  // ⚠️ Avoid logging OTP in prod

        return ResponseEntity.ok(Map.of("message", "OTP sent successfully", "otp", otp));
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String enteredOtp = request.get("otp");

        logger.info("OTP verification requested for email: {}", email);

        if (otpStorage.containsKey(email) && otpStorage.get(email).equals(enteredOtp)) {
            otpStorage.remove(email);

            Customer customer = customerService.findByEmail(email);
            if (customer == null) {
                logger.warn("OTP verification failed - customer not found for email: {}", email);
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }

//            String token = jwtUtil.generateToken(customer);
//            String email = customerJwtUtil.extractClaims(token).getSubject();
            String token = customerJwtUtil.generateToken(customer);  // Add this line before using 'token'

            ActiveToken activeToken = new ActiveToken();
            activeToken.setEmail(email);
            activeToken.setToken(token);
            activeTokenRepository.save(activeToken);

            logger.info("OTP verified successfully for {}. Token generated and login recorded.", email);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "customerId", customer.getCustomerId(),
                    "customerName", customer.getName()
            ));
        }

        logger.warn("Invalid OTP attempt for email: {}", email);
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid Authorization header"));
            }

            String token = authHeader.substring(7);

            // Use the injected instance to extract claims
            Claims claims = customerJwtUtil.extractClaims(token);
            String email = claims.getSubject();

            activeTokenRepository.deleteById(email);
            logger.info("Token removed from active session store for user: {}", email);

            Customer customer = customerService.findByEmail(email);
            if (customer == null) {
                logger.warn("Logout failed - user not found for email: {}", email);
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }

            logger.info("User logged out successfully: {}", email);
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));

        } catch (Exception e) {
            logger.error("Error during logout", e);
            return ResponseEntity.status(500).body(Map.of("message", "Logout failed due to server error"));
        }
    }
//    @PostMapping("/generate-otp")
//    public ResponseEntity<?> generateOtp(@RequestBody Map<String, String> request) {
//        String email = request.get("email");
//        Customer customer = customerService.findByEmail(email);
//        if (customer == null) {
//            return ResponseEntity.badRequest().body(Map.of("message", "Email not registered"));
//        }
//        // Generate 6-digit OTP
//        String otp = String.format("%06d", new Random().nextInt(999999));
//        // Store OTP temporarily (for demo purposes, use Redis in production)
//        otpStorage.put(email, otp);
//        //  Print OTP in server logs
//        System.out.println("Generated OTP for " + email + ": " + otp);
//        return ResponseEntity.ok(Map.of("message", "OTP sent successfully", "otp", otp));
//    }

//    @PostMapping("/verify-otp")
//    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
//        String email = request.get("email");
//        String enteredOtp = request.get("otp");
//        // Validate OTP
//        if (otpStorage.containsKey(email) && otpStorage.get(email).equals(enteredOtp)) {
//            otpStorage.remove(email); // Clear OTP after successful verification
//            // Fetch customer details
//            Customer customer = customerService.findByEmail(email);
//            if (customer == null) {
//                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
//            }
//            return ResponseEntity.ok(Map.of(
//                    "message", "OTP verified successfully",
//                    "customerId", customer.getCustomerId(),
//                    "customerName", customer.getName() // Assuming getName() returns full name
//            ));
//        }
//        return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP"));
//    }

    //  Fetch customer by ID
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
