
package com.bidding.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bidding.dto.UserBasicProfileDto;
import com.bidding.dto.UserLoginResponse;
import com.bidding.model.User;
import com.bidding.payload.MessageResponse;
import com.bidding.security.JwtUtil;
import com.bidding.security.limiter.LoginRateLimiter;
import com.bidding.security.limiter.OtpRateLimiter;
import com.bidding.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OtpRateLimiter otpRateLimiter;
    

    @Autowired
    private LoginRateLimiter loginRateLimiter;
    
    @Autowired
    private PasswordEncoder passwordEncoder;


    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerUser(
            @RequestParam("username") String username,
            @RequestParam("fullname") String fullname,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("mobile") String mobile,
            @RequestParam("aadhaarNo") String aadhaarNo,
            @RequestParam("aadhaarPhoto") MultipartFile aadhaarPhoto,
            @RequestParam("panNo") String panNo,
            @RequestParam("panPhoto") MultipartFile panPhoto,
            @RequestParam("is18Plus") boolean is18Plus
    ) {
        try {
            if (!otpRateLimiter.isAllowed(email)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new MessageResponse("Too many registration attempts. Try again later."));
            }
           
            if (userService.usernameExists(username)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new MessageResponse("Username already exists"));
            }
           
            if (userService.emailExists(email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new MessageResponse("Email already registered"));
            }

            ResponseEntity<?> aadhaarValidation = validateImageFile(aadhaarPhoto, "Aadhaar photo");
            if (aadhaarValidation != null) {
                return aadhaarValidation;
            }

            ResponseEntity<?> panValidation = validateImageFile(panPhoto, "PAN photo");
            if (panValidation != null) {
                return panValidation;
            }

            String aadhaarPhotoName = saveFile(aadhaarPhoto);
            String panPhotoName = saveFile(panPhoto);

            String otp = String.valueOf((int) (Math.random() * 9000) + 1000);

            User user = new User();
            user.setUsername(username);
            user.setFullname(fullname);
            user.setEmail(email);
            user.setPassword(password);
            user.setMobile(mobile);
            user.setAadhaarNo(aadhaarNo);
            user.setAadhaarPhoto(aadhaarPhotoName);
            user.setPanNo(panNo);
            user.setPanPhoto(panPhotoName);
            user.setIs18Plus(is18Plus);
            user.setIsVerified(false);
            user.setOtpVerified(false);
            user.setEmailOtp(otp);
            user.setEmailOtpExpiry(LocalDateTime.now().plusMinutes(5));
            user.setCreatedAt(LocalDateTime.now());

            sendOtpEmail(email, otp, "registration");
            userService.registerUser(user);

            return ResponseEntity.ok(new MessageResponse("Registration successful. OTP sent to your email."));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Failed to upload files"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Something went wrong: " + e.getMessage()));
        }
    }

    private ResponseEntity<?> validateImageFile(MultipartFile file, String fieldName) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(fieldName + " is required"));
        }

        String contentType = file.getContentType();
        if (contentType == null ||
            !(contentType.equals("image/jpeg") ||
             contentType.equals("image/jpg") ||
             contentType.equals("image/png"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(fieldName + " must be in JPG, JPEG, or PNG format"));
        }

        long maxFileSize = 5 * 1024 * 1024; // 2MB
        if (file.getSize() > maxFileSize) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(fieldName + " size must be less than 2MB"));
        }

        return null; 
    }
   
   
   
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String email, @RequestParam String otp) {
    if (!otpRateLimiter.isAllowed(email)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new MessageResponse("Too many OTP attempts. Try again later."));
        }

        Optional<User> optionalUser = userService.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
        }

        User user = optionalUser.get();
        if (user.getEmailOtp().equals(otp) && user.getEmailOtpExpiry().isAfter(LocalDateTime.now())) {
            user.setOtpVerified(true);
            userService.registerUser(user);
            return ResponseEntity.ok(new MessageResponse("OTP verified successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid or expired OTP"));
        }
    }
   


   
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String username, @RequestParam String password) {
    if (!loginRateLimiter.isAllowed(username)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new UserLoginResponse("Too many login attempts. Try again later.", null, null, null,null));
        }
       

        Optional<User> optionalUser = userService.login(username, password);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new UserLoginResponse("Invalid username or password", null, null, null, null));
        }

        User user = optionalUser.get();

        if (user.getIsBlocked()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new UserLoginResponse("Your account has been blocked by the admin.", null, null, null, null));
        }

        if (!user.getOtpVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new UserLoginResponse("Email not verified. Please verify your OTP.", null, null, null, null));
        }

        if (!user.getIsVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new UserLoginResponse("Your account is pending admin approval.", null, null, null, null));
        }

        //  Generate JWT Token
        String token = jwtUtil.generateToken(user.getUsername(), "USER");

        String greeting = generateWelcomeGreeting(user.getFullname());

        return ResponseEntity.ok(new UserLoginResponse(
                "Login successful!",
                user.getFullname(),
                greeting,
                token,
                user.getId()
        ));
    }

   
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header.");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromJwt(token);

            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }

            User user = userOpt.get();

            UserBasicProfileDto profile = new UserBasicProfileDto(
                    user.getFullname(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getMobile()
            );

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch profile: " + e.getMessage());
        }
    }

   
    @Transactional
    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteAccount(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.getUsernameFromJwt(token);

        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userOpt.get();

     
        sendAccountDeletionEmail(user.getEmail(), user.getFullname());

    
        userService.deleteUserByUsername(username);

        return ResponseEntity.ok(new MessageResponse("Your account has been deleted successfully. An email confirmation has been sent."));
    }
   
   
    private void sendAccountDeletionEmail(String toEmail, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Account Deletion Confirmation - Bidding Platform");
        message.setText("Dear " + fullName + ",\n\n" +
                "Your account has been permanently deleted from the Bidding Platform.\n\n" +
                "We're sorry to see you go. If you change your mind, feel free to register again anytime.\n\n" +
                "Best regards,\nTeam Bid Management");

        mailSender.send(message);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        return ResponseEntity.ok(new MessageResponse("You have been logged out successfully. Please discard your token."));
    }

   
   
    @PostMapping(value = "/forgot-username", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> forgotUsername(@RequestParam("email") String email) {
        try {
        
            if (!otpRateLimiter.isAllowed(email)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new MessageResponse("Too many requests. Please try again later."));
            }
           
      
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("No account found with this email."));
            }
           
           
            String otp = generateOtp();
            userService.setEmailOtp(email, otp, LocalDateTime.now().plusMinutes(5));
           
         
            sendOtpEmail(email, otp, "username_recovery");
           
            return ResponseEntity.ok(new MessageResponse("Verification OTP sent to your email."));
           
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Failed to process request. Please try again."));
        }
    }

    @PostMapping(value = "/verify-forgot-username-otp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> verifyForgotUsernameOtp(
            @RequestParam("email") String email,
            @RequestParam("otp") String otp) {
       
        try {
           
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("No account found with this email."));
            }
           
           
            if (!userService.verifyEmailOtp(email, otp)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Invalid or expired OTP."));
            }
           
          
            User user = userOpt.get();
            sendUsernameEmail(user.getEmail(), user.getUsername(), user.getFullname());
           
           
            userService.clearEmailOtp(email);
           
            return ResponseEntity.ok(new MessageResponse("Username sent to your registered email."));
           
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Failed to process request. Please try again."));
        }
    }

    // method to generate OTP
    private String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
    }
   
   
    private void sendUsernameEmail(String toEmail, String username, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Username Recovery - Bidding Platform");
        message.setText("Dear " + fullName + ",\n\n" +
                "Your username for the Bidding Platform is: " + username + "\n\n" +
                "If you didn't request this information, please contact our support team.\n\n" +
                "Best regards,\nTeam Bid Management");
        mailSender.send(message);
    }
   
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
    if (!otpRateLimiter.isAllowed(email)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many OTP requests. Please wait and try again later.");
        }
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email not found.");
        }
        String otp = String.valueOf((int) (Math.random() * 9000) + 1000);
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        userService.setForgotPasswordOtp(email, otp, expiry);
        sendForgotOtpEmail(email, otp);
        return ResponseEntity.ok("OTP sent to your email for password reset.");
    }

    @PostMapping("/verify-forgot-otp")
    public ResponseEntity<?> verifyForgotOtp(@RequestParam String email, @RequestParam String otp) {
    if (!otpRateLimiter.isAllowed(email)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many OTP verification attempts. Try again later.");
        }
        boolean isValid = userService.verifyForgotPasswordOtp(email, otp);
        if (isValid) {
            return ResponseEntity.ok("OTP verified. You can now reset your password.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired OTP.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        boolean success = userService.resetPassword(email, newPassword);
        if (success) {
            return ResponseEntity.ok("Password reset successful.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password reset failed.");
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        File dest = new File(dir, filename);
        file.transferTo(dest);
        return filename;
    }

    private void sendOtpEmail(String toEmail, String otp, String purpose) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
       
        switch(purpose.toLowerCase()) {
            case "registration":
                message.setSubject("Email OTP Verification - Bidding Platform");
                message.setText("Dear User,\n\nYour One-Time Password (OTP) for completing your registration is: " + otp +
                              "\n\nThis code is valid for 5 minutes.\nDo not share it with anyone.\n\nBest regards,\nTeam Bid Management");
                break;
               
            case "username_recovery":
                message.setSubject("OTP for Username Recovery - Bidding Platform");
                message.setText("Dear User,\n\nYour One-Time Password (OTP) for username recovery is: " + otp +
                              "\n\nThis OTP is valid for 5 minutes.\n\nIf you didn't request this, please contact our support team." +
                              "\n\nBest regards,\nTeam Bid Management");
                break;
               
            case "password_reset":
                message.setSubject("Password Reset OTP - Bidding Platform");
                message.setText("Hello,\n\nWe received a request to reset the password for your account." +
                              "\n\nYour One-Time Password (OTP) is:\n\n🔐 OTP: " + otp +
                              "\n\nThis OTP is valid for 5 minutes.\nPlease do not share it with anyone." +
                              "\n\nIf you did not request a password reset, please contact our support team." +
                              "\n\nBest regards,\nTeam Bid Management");
                break;
               
            default:
                message.setSubject("Your Verification OTP - Bidding Platform");
                message.setText("Your OTP is: " + otp + "\nValid for 5 minutes.");
        }
       
        mailSender.send(message);
    }

    private void sendForgotOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset OTP - Bidding Platform");
        message.setText("Hello,\n\nWe received a request to reset the password for your account.\n\nYour One-Time Password (OTP) is:\n\n🔐 OTP: " + otp + "\n\nThis OTP is valid for 5 minutes.\nPlease do not share it with anyone.\n\nIf you did not request a password reset, please ignore this email or contact our support team.\n\nBest regards,\nTeam Bid Management"
        );
        mailSender.send(message);
    }
   
    private String generateWelcomeGreeting(String name) {
        String timeGreeting;

        int hour = LocalDateTime.now().getHour();
        if (hour >= 5 && hour < 12) {
            timeGreeting = "Good Morning";
        } else if (hour >= 12 && hour < 17) {
            timeGreeting = "Good Afternoon";
        } else if (hour >= 17 && hour < 24) {
            timeGreeting = "Good Evening";
        } else {
            timeGreeting = "Welcome";
        }

        return timeGreeting + ", " + name + "! We're glad to see you 😊";
    }

}