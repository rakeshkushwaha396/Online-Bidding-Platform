package com.bidding.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    
    public void sendBidWinEmail(String toEmail, String productName, Double price) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Congratulations! You've won the bid");
        message.setText("Congratulations!\n\nYou have successfully won the auction for the product: " + productName + ".\n\nFinal Price: ₹" + price + "\n\nPlease proceed to your dashboard to view the order details.\n\nBest regards,\nTeam Bid Management"
);
        mailSender.send(message);
    }
}
