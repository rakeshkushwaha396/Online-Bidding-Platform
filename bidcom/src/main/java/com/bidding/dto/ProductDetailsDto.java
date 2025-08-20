package com.bidding.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data

public class ProductDetailsDto {
    private Long productId;
    private String name;
    private String category;
    private String description;
    private Double basePrice;
    private Double currentBid;
    private Long totalBids;
    private String remainingTime;
    private String status;
    private String sellerName;
    private String sellerMobile;
    private String imageUrl;
    
    
    
	public ProductDetailsDto(Long productId, String name, String category, String description, Double basePrice,
			Double currentBid, Long totalBids, String remainingTime, String status, String sellerName,
			String sellerMobile, String imageUrl) {
		super();
		this.productId = productId;
		this.name = name;
		this.category = category;
		this.description = description;
		this.basePrice = basePrice;
		this.currentBid = currentBid;
		this.totalBids = totalBids;
		this.remainingTime = remainingTime;
		this.status = status;
		this.sellerName = sellerName;
		this.sellerMobile = sellerMobile;
		this.imageUrl = imageUrl;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
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
	public Double getCurrentBid() {
		return currentBid;
	}
	public void setCurrentBid(Double currentBid) {
		this.currentBid = currentBid;
	}
	public Long getTotalBids() {
		return totalBids;
	}
	public void setTotalBids(Long totalBids) {
		this.totalBids = totalBids;
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
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
    
    
}
