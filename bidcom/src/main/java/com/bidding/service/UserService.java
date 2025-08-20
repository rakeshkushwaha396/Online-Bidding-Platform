

package com.bidding.service;

import com.bidding.model.Product;
import com.bidding.model.User;
import com.bidding.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JavaMailSender mailSender;

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public User registerUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    
    public Optional<User> login(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password);
    }

    public void setForgotPasswordOtp(String email, String otp, LocalDateTime expiry) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        userOpt.ifPresent(user -> {
            user.setForgotPasswordOtp(otp);
            user.setForgotPasswordOtpExpiry(expiry);
            userRepository.save(user);
        });
    }

    public boolean verifyForgotPasswordOtp(String email, String otp) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        return optionalUser.map(user -> {
            String storedOtp = user.getForgotPasswordOtp();
            LocalDateTime expiry = user.getForgotPasswordOtpExpiry();

            if (storedOtp == null || expiry == null) return false;

            return storedOtp.equals(otp) && expiry.isAfter(LocalDateTime.now());
        }).orElse(false);
    }


    public boolean resetPassword(String email, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(newPassword);
            user.setForgotPasswordOtp(null);
            user.setForgotPasswordOtpExpiry(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }
    
    public void sendWinningEmail(User winner, Product product) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(winner.getEmail());
        message.setSubject("🎉 You Won the Auction!");
        message.setText("Congratulations " + winner.getFullname() + ",\n\nYou won the auction for: " + product.getName() +
                "\nYour winning bid: ₹" + product.getFinalPrice() +
                "\n\nPlease check your dashboard for more details.");
        mailSender.send(message);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public void deleteUserByUsername(String username) {
        userRepository.deleteByUsername(username);
    }

    public void setEmailOtp(String email, String otp, LocalDateTime expiry) {

        Optional<User> userOpt = userRepository.findByEmail(email);

        userOpt.ifPresent(user -> {

            user.setEmailOtp(otp);

            user.setEmailOtpExpiry(expiry);

            userRepository.save(user);

        });

    }



    public boolean verifyEmailOtp(String email, String otp) {

        Optional<User> optionalUser = userRepository.findByEmail(email);

        return optionalUser.map(user -> {

            String storedOtp = user.getEmailOtp();

            LocalDateTime expiry = user.getEmailOtpExpiry();



            if (storedOtp == null || expiry == null) return false;



            return storedOtp.equals(otp) && expiry.isAfter(LocalDateTime.now());

        }).orElse(false);

    }



    public void clearEmailOtp(String email) {

        Optional<User> userOpt = userRepository.findByEmail(email);

        userOpt.ifPresent(user -> {

            user.setEmailOtp(null);

            user.setEmailOtpExpiry(null);

            userRepository.save(user);

        });

    }



    public void saveUser(User user) {

        userRepository.save(user);

    }
}
