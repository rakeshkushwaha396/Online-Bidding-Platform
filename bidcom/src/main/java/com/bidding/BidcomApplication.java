package com.bidding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BidcomApplication {

	public static void main(String[] args) {
		SpringApplication.run(BidcomApplication.class, args);
	}

}
