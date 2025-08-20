package com.bidding.repository;

import com.bidding.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameAndPassword(String username, String password);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByIsVerifiedFalse();
    List<User> findByIsVerifiedFalseAndOtpVerifiedTrue();
    
    List<User> findByIsVerifiedFalseAndOtpVerifiedTrueAndCreatedAtBefore(LocalDateTime cutoff);

    void deleteByUsername(String username);


    

}
