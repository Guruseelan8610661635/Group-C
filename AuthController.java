package com.smartparking.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.smartparking.model.Role;
import com.smartparking.model.User;
import com.smartparking.model.Vehicle;
import com.smartparking.repository.UserRepository;
import com.smartparking.repository.VehicleRepository;
import com.smartparking.security.JwtUtil;
import com.smartparking.service.OTPService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private UserRepository repo;
    
    @Autowired
    private VehicleRepository vehicleRepo;

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OTPService otpService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {

        // Check if email already exists
        if (repo.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Email already registered"));
        }

        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        user.setPassword(encoder.encode(user.getPassword()));
        
        User savedUser = repo.save(user);

        // ✅ FIX: Return complete user data
        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUser.getId());
        response.put("email", savedUser.getEmail());
        response.put("name", savedUser.getName());
        response.put("phone", savedUser.getPhone());
        response.put("role", savedUser.getRole().name());
        response.put("createdAt", savedUser.getCreatedAt());
        response.put("message", "User registered successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {

        User user = repo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(401)
                    .body("Invalid Credentials");
        }

        if (user.getRole() == null) {
            user.setRole(Role.USER);
            repo.save(user);
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        // ✅ FIX: Return complete user data
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("role", user.getRole().name());
        response.put("expiresIn", 86400);  // 24 hours in seconds

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Unauthorized - No authentication"));
            }
            
            String email = authentication.getName();
            User user = repo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

            // Get user's vehicles
            List<Vehicle> vehicles = vehicleRepo.findByUserId(user.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("phone", user.getPhone());
            response.put("role", user.getRole() != null ? user.getRole().name() : "USER");
            response.put("createdAt", user.getCreatedAt());
            
            // Convert vehicles to simplified format
            List<Map<String, Object>> vehicleList = vehicles.stream()
                    .map(v -> {
                        Map<String, Object> vMap = new HashMap<>();
                        vMap.put("id", v.getId());
                        vMap.put("vehicleType", v.getVehicleType() != null ? v.getVehicleType().name() : "CAR");
                        vMap.put("registrationNumber", v.getRegistrationNumber());
                        // Include all optional fields so the frontend can render full vehicle info
                        vMap.put("isDefault", v.getIsDefault());
                        vMap.put("make", v.getMake());
                        vMap.put("model", v.getModel());
                        vMap.put("color", v.getColor());
                        return vMap;
                    })
                    .collect(Collectors.toList());
            
            response.put("vehicles", vehicleList);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage(), "type", e.getClass().getSimpleName()));
        }
    }

    /**
     * Admin Login Step 1: Verify email/password and send OTP
     */
    @PostMapping("/admin/login-request-otp")
    public ResponseEntity<?> adminLoginRequestOTP(@RequestBody User request) {
        try {
            // Find user by email
            User user = repo.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            // Verify password
            if (!encoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Invalid credentials"));
            }

            // Check if user is admin
            if (user.getRole() != Role.ADMIN) {
                return ResponseEntity.status(403)
                        .body(Map.of("error", "Access denied. Admin privileges required."));
            }

            // Generate and send OTP
            boolean otpSent = otpService.generateAndSendOTP(user.getEmail());

            if (!otpSent) {
                return ResponseEntity.status(500)
                        .body(Map.of("error", "Failed to send OTP. Please try again."));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "OTP sent to your email");
            response.put("email", user.getEmail());
            response.put("requiresOTP", true);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "An error occurred during login"));
        }
    }

    /**
     * Admin Login Step 2: Verify OTP and issue JWT token
     */
    @PostMapping("/admin/verify-otp")
    public ResponseEntity<?> adminVerifyOTP(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");

            if (email == null || otp == null) {
                return ResponseEntity.status(400)
                        .body(Map.of("error", "Email and OTP are required"));
            }

            // Verify OTP
            boolean otpValid = otpService.verifyOTP(email, otp);

            if (!otpValid) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Invalid or expired OTP"));
            }

            // Get user details
            User user = repo.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("role", user.getRole().name());
            response.put("expiresIn", 86400);

            System.out.println("✅ Admin " + user.getEmail() + " logged in successfully with 2FA");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "An error occurred during OTP verification"));
        }
    }

    /**
     * Get all users (Admin only)
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = repo.findAll();
            
            List<Map<String, Object>> usersList = users.stream()
                    .map(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", user.getId());
                        userMap.put("email", user.getEmail());
                        userMap.put("name", user.getName());
                        userMap.put("phone", user.getPhone());
                        userMap.put("role", user.getRole() != null ? user.getRole().name() : "USER");
                        userMap.put("createdAt", user.getCreatedAt());
                        return userMap;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(usersList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to fetch users: " + e.getMessage()));
        }
    }
}
