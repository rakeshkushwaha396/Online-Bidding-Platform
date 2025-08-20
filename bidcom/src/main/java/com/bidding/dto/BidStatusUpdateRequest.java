package com.bidding.dto;



import lombok.Data;



@Data

public class BidStatusUpdateRequest {

    private Long productId;

    private Long userId;

    private String status; // "WON" or "LOST"

}