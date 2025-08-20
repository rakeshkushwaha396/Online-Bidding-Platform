package com.bidding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BidUpdateDto {
    private Long productId;
    private String productName;
    private Double newBidAmount;
    private Double currentHighestBid;
    private String bidderName;
    private String bidderUsername;
    private Integer totalBids;
    private String remainingTime;
}