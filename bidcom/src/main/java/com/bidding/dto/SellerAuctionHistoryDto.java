package com.bidding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerAuctionHistoryDto {

    private Long productId;
    private String productName;
    private Double basePrice;
    private String imageUrl;
    private String category;
    private long totalBids;
    private Double finalPrice;
    private String bidStatus;      
    private String buyerName;      
    private String buyerMobile;    
    private String remainingTime;
    private Double currentBidPrice;
    
    
    
	
	
	
	
	public Long getProductId() {
		return productId;
	}
	public Double getCurrentBidPrice() {
		return currentBidPrice;
	}


	public void setCurrentBidPrice(Double currentBidPrice) {
		this.currentBidPrice = currentBidPrice;
	}


	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public Double getBasePrice() {
		return basePrice;
	}
	public void setBasePrice(Double basePrice) {
		this.basePrice = basePrice;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public long getTotalBids() {
		return totalBids;
	}
	public void setTotalBids(long totalBids) {
		this.totalBids = totalBids;
	}
	public Double getFinalPrice() {
		return finalPrice;
	}
	public void setFinalPrice(Double finalPrice) {
		this.finalPrice = finalPrice;
	}
	public String getBidStatus() {
		return bidStatus;
	}
	public void setBidStatus(String bidStatus) {
		this.bidStatus = bidStatus;
	}
	public String getBuyerName() {
		return buyerName;
	}
	public void setBuyerName(String buyerName) {
		this.buyerName = buyerName;
	}
	public String getBuyerMobile() {
		return buyerMobile;
	}
	public void setBuyerMobile(String buyerMobile) {
		this.buyerMobile = buyerMobile;
	}
	public String getRemainingTime() {
		return remainingTime;
	}
	public void setRemainingTime(String remainingTime) {
		this.remainingTime = remainingTime;
	}
    
    
}
