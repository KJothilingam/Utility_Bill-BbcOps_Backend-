package com.BBC_Ops.BBC_Ops.Controller;


import com.BBC_Ops.BBC_Ops.Model.ActiveToken;
import com.BBC_Ops.BBC_Ops.Model.Employee;
import com.BBC_Ops.BBC_Ops.Repository.ActiveTokenRepository;
import com.BBC_Ops.BBC_Ops.Repository.EmployeeRepository;
import com.BBC_Ops.BBC_Ops.Service.AuditService;
import com.BBC_Ops.BBC_Ops.Service.EmployeeService;
import com.BBC_Ops.BBC_Ops.configuration.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("employees")
public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private JwtUtil jwtUtil;

    private Map<String, String> otpStorage = new HashMap<>();

    @PostMapping("/generate-otp")
    public ResponseEntity<?> generateOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        logger.info("OTP generation requested for email: {}", email);

        Employee employee = employeeService.findByEmail(email);
        if (employee == null) {
            logger.warn("OTP generation failed - email not registered: {}", email);
            return ResponseEntity.badRequest().body(Map.of("message", "Email not registered"));
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);

        logger.info("Generated OTP for {}: {}", email, otp); // Note: In production, avoid logging the OTP
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully", "otp", otp));
    }

    @Autowired
    private ActiveTokenRepository activeTokenRepository;

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String enteredOtp = request.get("otp");

        logger.info("OTP verification requested for email: {}", email);

        if (otpStorage.containsKey(email) && otpStorage.get(email).equals(enteredOtp)) {
            otpStorage.remove(email);
            Employee employee = employeeService.findByEmail(email);

            if (employee == null) {
                logger.warn("OTP verification failed - employee not found for email: {}", email);
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }

            String token = jwtUtil.generateToken(employee);

            ActiveToken activeToken = new ActiveToken();
            activeToken.setEmail(email);
            activeToken.setToken(token);
            activeTokenRepository.save(activeToken);

            auditService.logAction(employee, "Logged In via OTP");
            logger.info("OTP verified successfully for {}. Token generated and login recorded.", email);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", employee.getEmployeeId(),
                    "userName", employee.getName(),
                    "designation", employee.getDesignation()
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

            String token = authHeader.substring(7); // remove "Bearer "
            String email = jwtUtil.extractClaims(token).getSubject();

            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid token"));
            }

            activeTokenRepository.deleteById(email);
            logger.info("Token removed from active session store for user: {}", email);

            Employee employee = employeeService.findByEmail(email);
            if (employee == null) {
                logger.warn("Logout failed - user not found for email: {}", email);
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }

            auditService.logAction(employee, "Logged Out");
            logger.info("User logged out successfully: {}", email);

            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));

        } catch (Exception e) {
            logger.error("Error during logout", e);
            return ResponseEntity.status(500).body(Map.of("message", "Logout failed due to server error"));
        }
    }

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


    @GetMapping("/data/{id}")
    public ResponseEntity<?> getEmployeeDataById(@PathVariable Long id) {
        Employee employee = employeeService.findById(id);
        if (employee != null) {
            return ResponseEntity.ok(Map.of(
                    "employee_id", employee.getEmployeeId(),
                    "name", employee.getName(),
                    "dob", employee.getDob(),
                    "email", employee.getEmail(),
                    "phone_number", employee.getPhoneNumber(),
                    "designation", employee.getDesignation()
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Employee not found"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        Optional<Employee> optionalEmployee = employeeService.getEmployeeById(id);

        if (!optionalEmployee.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Employee existingEmployee = optionalEmployee.get();

        // Update only email and phone number
        if (employee.getEmail() != null) {
            existingEmployee.setEmail(employee.getEmail());
        }
        if (employee.getPhoneNumber() != null) {
            existingEmployee.setPhoneNumber(employee.getPhoneNumber());
        }

        Employee updatedEmployee = employeeService.saveEmployee(existingEmployee);
        return ResponseEntity.ok(updatedEmployee);
    }



}