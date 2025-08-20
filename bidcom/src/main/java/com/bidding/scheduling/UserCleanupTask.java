package com.bidding.scheduling;

import com.bidding.model.User;
import com.bidding.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component

public class UserCleanupTask {

    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    public UserCleanupTask(UserRepository userRepository, JavaMailSender mailSender) {
		super();
		this.userRepository = userRepository;
		this.mailSender = mailSender;
	}

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void deleteUnapprovedUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        List<User> staleUsers = userRepository
                .findByIsVerifiedFalseAndOtpVerifiedTrueAndCreatedAtBefore(cutoff);

        for (User user : staleUsers) {
            sendRejectionEmail(user.getEmail());
            userRepository.delete(user);
        }
    }

    private void sendRejectionEmail(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Account Not Approved - Bidding Platform");
        message.setText("Dear User,\n\nYour account was not approved by the admin within 24 hours.\nWe kindly request you to register again with valid details.\n\nBest regards,\nTeam\nBid Management"
);
        mailSender.send(message);
    }
}
