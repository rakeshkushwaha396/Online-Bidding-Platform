package com.bidding.dto;

import com.bidding.model.User;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor

public class UserSummaryDto {
    private Long id;
    private String fullname;
    private String email;
    private Boolean isVerified;
    private Boolean isBlocked;

    public UserSummaryDto(User user) {
        this.id = user.getId();
        this.fullname = user.getFullname();
        this.email = user.getEmail();
        this.isVerified = user.getIsVerified();
        this.isBlocked = user.getIsBlocked();
    }

	

	
}
