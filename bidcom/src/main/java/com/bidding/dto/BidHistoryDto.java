package com.bidding.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor

public class BidHistoryDto {
    private Long productId;
    private String productName;
    private String imageUrl;
    private String category;
    private Double basePrice;
    private Double yourBidPrice;
    private Double finalPrice;
    private String remainingTime;
    private String status;    
    private String bidStatus;
    private Double currentBidPrice;
    private String sellerName;  // or sellerUsername
    private String sellerPhone;
   




public Double getCurrentBidPrice() {
return currentBidPrice;
}


public void setCurrentBidPrice(Double currentBidPrice) {
this.currentBidPrice = currentBidPrice;
}


public Long getProductId() {
return productId;
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
public Double getBasePrice() {
return basePrice;
}
public void setBasePrice(Double basePrice) {
this.basePrice = basePrice;
}
public Double getYourBidPrice() {
return yourBidPrice;
}
public void setYourBidPrice(Double yourBidPrice) {
this.yourBidPrice = yourBidPrice;
}
public Double getFinalPrice() {
return finalPrice;
}
public void setFinalPrice(Double finalPrice) {
this.finalPrice = finalPrice;
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
public String getBidStatus() {
return bidStatus;
}
public void setBidStatus(String bidStatus) {
this.bidStatus = bidStatus;
}


   
}