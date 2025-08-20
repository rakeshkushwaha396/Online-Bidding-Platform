package com.bidding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BidUpdate {
    private Long productId;
    private Double currentPrice;
    private String bidderName;
    private Integer totalBids;
   
    public BidUpdate(Long productId, Double currentPrice) {
        this.productId = productId;
        this.currentPrice = currentPrice;
        this.bidderName = "Anonymous"; 
        this.totalBids = 0; 
    }
}