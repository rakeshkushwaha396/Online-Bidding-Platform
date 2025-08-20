package com.bidding.repository;

import com.bidding.model.Bid;
import com.bidding.model.Product;
import com.bidding.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByProduct(Product product);
    List<Bid> findByUser(User user);

  
    @Query("SELECT MAX(b.bidPrice) FROM Bid b WHERE b.product.id = :productId")
    Double findHighestBidAmount(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(b) FROM Bid b WHERE b.product.id = :productId")
    Long countByProductId(@Param("productId") Long productId);
    
    List<Bid> findByUserIdOrderByBidTimeDesc(Long userId);

    @Query("SELECT b FROM Bid b JOIN FETCH b.product p JOIN FETCH p.seller WHERE b.user.id = :userId")
    List<Bid> findByUserIdWithSeller(@Param("userId") Long userId);


    
    @Query("SELECT b FROM Bid b WHERE b.product.id = :productId ORDER BY b.bidPrice DESC LIMIT 1")
    Bid findHighestBidByProductId(@Param("productId") Long productId);


    @Query("SELECT b FROM Bid b WHERE b.product.seller.id = :sellerId AND b.product.status = 'ENDED' AND b.bidPrice = (SELECT MAX(b2.bidPrice) FROM Bid b2 WHERE b2.product = b.product)")
    List<Bid> findWinningBidsBySeller(@Param("sellerId") Long sellerId);

    
    @Query("SELECT b FROM Bid b WHERE b.user.id = :userId AND b.product.status = 'ENDED' AND b.bidPrice = (SELECT MAX(b2.bidPrice) FROM Bid b2 WHERE b2.product = b.product)")
    List<Bid> findWinningBidsByUser(@Param("userId") Long userId);
    @Query("SELECT b FROM Bid b WHERE b.product.id = :productId AND b.id <> :excludedId")

    List<Bid> findByProductIdExcludingBid(

        @Param("productId") Long productId, 

        @Param("excludedId") Long excludedId

    );

    

    

    List<Bid> findByProductId(Long productId);
    

    @Query("SELECT b FROM Bid b WHERE b.product = :product ORDER BY b.bidPrice DESC")
    List<Bid> findTopBidForProduct(@Param("product") Product product);
  
    
    @Query("SELECT b FROM Bid b WHERE b.user.id = :userId")
    List<Bid> findAllBidsByUser(@Param("userId") Long userId);

    
    List<Bid> findByUserId(Long userId);
    Bid findTopByProductIdOrderByBidPriceDesc(Long productId);
   

}
