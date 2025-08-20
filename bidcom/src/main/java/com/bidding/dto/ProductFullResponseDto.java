package com.bidding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@AllArgsConstructor
public class ProductFullResponseDto {
    private Long id;
    private String name;
    private String description;
    private Double basePrice;
    private String imageUrl;
    private String sellerName;
    private String sellerMobile;
    private String category;
    private String auctionStart;
    private String auctionEnd;
    private String remainingTime;
    private String status; 
    
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	public String getSellerName() {
		return sellerName;
	}
	public void setSellerName(String sellerName) {
		this.sellerName = sellerName;
	}
	public String getSellerMobile() {
		return sellerMobile;
	}
	public void setSellerMobile(String sellerMobile) {
		this.sellerMobile = sellerMobile;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getAuctionStart() {
		return auctionStart;
	}
	public void setAuctionStart(String auctionStart) {
		this.auctionStart = auctionStart;
	}
	public String getAuctionEnd() {
		return auctionEnd;
	}
	public void setAuctionEnd(String auctionEnd) {
		this.auctionEnd = auctionEnd;
	}
	public String getRemainingTime() {
		return remainingTime;
	}
	public void setRemainingTime(String remainingTime) {
		this.remainingTime = remainingTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	    
}
