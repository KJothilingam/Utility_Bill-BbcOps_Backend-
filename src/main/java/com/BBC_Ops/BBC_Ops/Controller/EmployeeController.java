package com.BBC_Ops.BBC_Ops.Controller;


import com.BBC_Ops.BBC_Ops.Model.Employee;
import com.BBC_Ops.BBC_Ops.Service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("employees")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    private Map<String, String> otpStorage = new HashMap<>();

    @PostMapping("/generate-otp")
    public ResponseEntity<?> generateOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        // Check if email exists in DB
        Employee employee = employeeService.findByEmail(email);
        if (employee == null) {
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
            return ResponseEntity.ok(Map.of("message", "OTP verified successfully"));
        }

        return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP"));
    }

//
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        Optional<Employee> employee = employeeService.getEmployeeById(id);
        return employee.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Employee createEmployee(@RequestBody Employee employee) {
        return employeeService.createEmployee(employee);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}