package com.bidding.repository;

import com.bidding.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(String status);
    List<Product> findByStatusAndAuctionEndBefore(String status, LocalDateTime now);

    List<Product> findByStatusAndCategory(String status, String category);
   
    List<Product> findBySellerId(Long sellerId); 

    @Query("SELECT p FROM Product p WHERE p.auctionEnd > CURRENT_TIMESTAMP")
    List<Product> findAllActiveProducts();
}
