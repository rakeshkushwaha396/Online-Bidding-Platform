package com.bidding.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;

@AllArgsConstructor

public class ProductResponseDto implements Serializable {

    private Long id;
    private String name;
    private String description;
    private Double basePrice;
    private String imageUrl;

    public ProductResponseDto() {
    }


    

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
}
