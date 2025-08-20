
package com.bidding.controller;

import com.bidding.dto.UserSummaryDto;
import com.bidding.model.Admin;
import com.bidding.model.User;
import com.bidding.repository.AdminRepository;
import com.bidding.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "http://localhost:3000")

public class AdminController {

    public AdminController(UserRepository userRepository, AdminRepository adminRepository, JavaMailSender mailSender) {
		super();
		this.userRepository = userRepository;
		this.adminRepository = adminRepository;
		this.mailSender = mailSender;
	}

	private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final JavaMailSender mailSender;

    @PostMapping(value = "/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> loginAdmin(
            @RequestParam("username") String username,
            @RequestParam("password") String password) {

        Admin admin = adminRepository.findByUsernameAndPassword(username, password).orElse(null);
        System.out.println("Admin found: " + admin);

        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }

        admin.setPassword(null); // Hide password in response
        return ResponseEntity.ok(admin);
    }

    @GetMapping("/users")
    public List<User> getPendingUsers() {
        return userRepository.findByIsVerifiedFalseAndOtpVerifiedTrue();
    }
    
    @GetMapping("/users/all")
    public ResponseEntity<List<UserSummaryDto>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserSummaryDto> summaries = users.stream()
                .map(UserSummaryDto::new)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    @PostMapping("/verify/{id}")
    public ResponseEntity<String> verifyUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            user.setIsVerified(true);
            userRepository.save(user);

            //  Send approval email
            sendApprovalEmail(user.getEmail(), user.getFullname());

            return ResponseEntity.ok("User verified successfully.");
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found."));
    }

    @PostMapping("/block-user/{id}")
    public ResponseEntity<String> blockUser(
            @PathVariable Long id,
            @RequestParam boolean block
    ) {
        return userRepository.findById(id).map(user -> {
            user.setIsBlocked(block);
            userRepository.save(user);

            if (block) {
                sendBlockEmail(user.getEmail(), user.getFullname());
                return ResponseEntity.ok("User has been blocked successfully.");
            } else {
                sendUnblockEmail(user.getEmail(), user.getFullname());
                return ResponseEntity.ok("User has been unblocked successfully.");
            }
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found."));
    }
    private void sendBlockEmail(String toEmail, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Account Blocked - Bidding Platform");
        message.setText("Dear " + fullName + ",\n\n" +
                "Your account has been temporarily blocked by the administrator.\n\n" +
                "You will not be able to log in or participate in auctions until it is unblocked.\n\n" +
                "If you believe this was a mistake, please contact our support team.\n\n" +
                "Best regards,\nTeam Bid Management");
        mailSender.send(message);
    }

    private void sendUnblockEmail(String toEmail, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Account Unblocked - Bidding Platform");
        message.setText("Dear " + fullName + ",\n\n" +
                "Your account has been unblocked by the administrator.\n\n" +
                "You can now log in and continue using the platform.\n\n" +
                "Best regards,\nTeam Bid Management");
        mailSender.send(message);
    }
    
    @PostMapping("/reject/{id}")
    public ResponseEntity<String> rejectUser(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            // Send rejection email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Account Rejected - Bidding Platform");
            message.setText("Dear" + ",\n\n" +
            		"We regret to inform you that your account could not be approved due to incomplete or invalid information.\n\n" +
            		"We kindly request you to re-register with accurate and valid details to proceed further.\n\n" +
            		"If you have any questions or need assistance, feel free to contact our support team.\n\n" +
            		"Best regards,\n" +
            		"Team Bid Management"
);
            mailSender.send(message);

            // Delete user
            userRepository.delete(user);

            return ResponseEntity.ok("User rejected, deleted, and email sent.");
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found."));
    }

    private void sendApprovalEmail(String toEmail, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Bidding Platform - Account Approved");
        message.setText("Dear " + fullName + ",\n\n" +
        		"Congratulations! Your account has been successfully approved by our administrative team.\n\n" +
        		"You can now log in to your account and start participating in auctions.\n\n" +
        		"If you have any questions or need assistance, feel free to reach out to our support team.\n\n" +
        		"Best regards,\n" +
        		"Team Bid Management");
        mailSender.send(message);
    }
    
    
}

