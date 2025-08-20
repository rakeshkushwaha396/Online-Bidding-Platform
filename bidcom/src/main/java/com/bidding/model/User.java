package com.bidding.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    private String fullname;
    private String password;
    private String mobile;

    @Column(name = "aadhaar_no")
    private String aadhaarNo;

    @Column(name = "aadhaar_photo")
    private String aadhaarPhoto;

    @Column(name = "pan_no")
    private String panNo;

    @Column(name = "pan_photo")
    private String panPhoto;

    private Boolean is18Plus;
    
    @Column(name = "verified",nullable = false)
    private Boolean isVerified;

    @Column(name = "email_otp")
    private String emailOtp;

    @Column(name = "email_otp_expiry")
    private LocalDateTime emailOtpExpiry;

    @Column(name = "otp_verified")
    private Boolean otpVerified;

    @Column(name = "forgot_password_otp")
    private String forgotPasswordOtp;

    @Column(name = "forgot_password_otp_expiry")
    private LocalDateTime forgotPasswordOtpExpiry;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "is_blocked", nullable = false)
    private Boolean isBlocked = false;

    
}
