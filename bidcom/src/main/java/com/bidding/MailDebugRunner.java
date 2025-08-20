package com.bidding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class MailDebugRunner implements CommandLineRunner {

    @Autowired
    private Environment env;

    @Override
    public void run(String... args) {
        System.out.println("== MAIL CONFIG TEST ==");
        System.out.println("spring.mail.host = " + env.getProperty("spring.mail.host"));
        System.out.println("spring.mail.username = " + env.getProperty("spring.mail.username"));
        System.out.println("Mail Port: " + env.getProperty("spring.mail.port"));
    }
}
