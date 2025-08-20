package com.bidding.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bidding.dto.BidHistoryDto;
import com.bidding.dto.BidRequestDto;
import com.bidding.dto.BidStatusUpdateRequest;
import com.bidding.dto.BidUpdateDto;
import com.bidding.dto.ProductPriceDto;
import com.bidding.dto.SellerAuctionHistoryDto;
import com.bidding.model.Bid;
import com.bidding.model.Product;
import com.bidding.model.User;
import com.bidding.repository.BidRepository;
import com.bidding.repository.ProductRepository;
import com.bidding.repository.UserRepository;
import com.bidding.security.JwtUtil;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/bids")
@AllArgsConstructor
public class BidController {

    private final BidRepository bidRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/count/{productId}")
    public ResponseEntity<?> getBidCountForProduct(@PathVariable Long productId) {
        if (!productRepository.existsById(productId)) {
            return ResponseEntity.badRequest().body("Invalid product ID");
        }

        Long count = bidRepository.countByProductId(productId);
        return ResponseEntity.ok("Total bids for product " + productId + ": " + count);
    }

    @GetMapping("/price/{productId}")
    public ResponseEntity<?> getProductPriceInfo(@PathVariable Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return ResponseEntity.badRequest().body("Invalid product ID");
        }

        Double highestBid = bidRepository.findHighestBidAmount(productId);
        if (highestBid == null) {
            highestBid = product.getBasePrice(); // No bids yet
        }

        ProductPriceDto response = new ProductPriceDto(
                product.getId(),
                product.getBasePrice(),
                highestBid
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/place", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> placeBid(@ModelAttribute BidRequestDto bidDto,
                                         @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromJwt(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user == null) return ResponseEntity.badRequest().body("Invalid user");

        Product product = productRepository.findById(bidDto.getProductId()).orElse(null);
        if (product == null) return ResponseEntity.badRequest().body("Invalid product ID");

        LocalDateTime now = LocalDateTime.now();
        if (!"LIVE".equals(product.getStatus()) ||
                now.isBefore(product.getAuctionStart()) ||
                now.isAfter(product.getAuctionEnd())) {
            return ResponseEntity.badRequest().body("Bidding is not allowed at this time.");
        }

        Double currentHighest = bidRepository.findHighestBidAmount(product.getId());
        if (currentHighest == null) currentHighest = product.getBasePrice();

        if (bidDto.getBidAmount() <= currentHighest) {
            return ResponseEntity.badRequest().body("Bid must be higher than current highest bid (" + currentHighest + ")");
        }

        Bid bid = new Bid();
        bid.setProduct(product);
        bid.setUser(user);
        bid.setBidPrice(bidDto.getBidAmount());
        bid.setBidTime(now);
        bidRepository.save(bid);

        product.setFinalPrice(bidDto.getBidAmount());
        productRepository.save(product);

        broadcastBidUpdate(product, user, bidDto.getBidAmount());

        return ResponseEntity.ok("Bid placed successfully!");
    }

    private void broadcastBidUpdate(Product product, User user, Double bidAmount) {
        Long totalBids = bidRepository.countByProductId(product.getId());
        Double highestBid = bidRepository.findHighestBidAmount(product.getId());
       
        BidUpdateDto update = new BidUpdateDto(
                product.getId(),
                product.getName(),
                bidAmount,
                highestBid != null ? highestBid : product.getBasePrice(),
                user.getFullname(),
                user.getUsername(),
                totalBids.intValue(),
                calculateRemainingTime(product.getAuctionEnd())
        );

        messagingTemplate.convertAndSend("/topic/bids/" + product.getId(), update);
        messagingTemplate.convertAndSend("/topic/bids/latest", update);
    }

    private String calculateRemainingTime(LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endTime)) {
            return "00:00:00";
        }
        Duration duration = Duration.between(now, endTime);
        return String.format("%02d:%02d:%02d",
                duration.toHours(),
                duration.toMinutes() % 60,
                duration.getSeconds() % 60);
    }

//    @GetMapping("/bid-history")
//    public ResponseEntity<List<BidHistoryDto>> getUserBidHistory(
//            @RequestHeader("Authorization") String authHeader,
//            @RequestParam(required = false) String status) {
//       
//        String token = authHeader.substring(7);
//        String username = jwtUtil.getUsernameFromJwt(token);
//        User user = userRepository.findByUsername(username).orElse(null);
//        if (user == null) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        List<Bid> userBids = bidRepository.findByUserId(user.getId());
//
//        Map<Long, Bid> highestBidsPerProduct = userBids.stream()
//            .collect(Collectors.toMap(
//                bid -> bid.getProduct().getId(),
//                bid -> bid,
//                (existing, replacement) -> existing.getBidPrice() > replacement.getBidPrice() ? existing : replacement
//            ));
//
//        LocalDateTime now = LocalDateTime.now();
//
//        List<BidHistoryDto> history = highestBidsPerProduct.values().stream().map(bid -> {
//            Product product = bid.getProduct();
//            User seller = product.getSeller();
//           
//            Double finalPrice = product.getFinalPrice();
//            LocalDateTime endTime = product.getAuctionEnd();
//
//            Double currentBidPrice = bidRepository.findHighestBidAmount(product.getId());
//            if (currentBidPrice == null) {
//                currentBidPrice = product.getBasePrice();
//            }
//
//            String bidStatus = now.isAfter(endTime) ? "EXPIRED" : "LIVE";
//
//            String winStatus = "ONHOLD";
//            if ("EXPIRED".equals(bidStatus)) {
//                winStatus = (finalPrice != null && Double.compare(bid.getBidPrice(), finalPrice) == 0) ? "WON" : "LOST";
//            }
//
//            String remainingTime = calculateRemainingTime(endTime);
//
//            return new BidHistoryDto(
//                    product.getId(),
//                    product.getName(),
//                    "/uploads/" + product.getImage(),
//                    product.getCategory(),
//                    product.getBasePrice(),
//                    bid.getBidPrice(),
//                    finalPrice,
//                    remainingTime,
//                    winStatus,
//                    bidStatus,
//                    currentBidPrice,
//                    seller.getUsername(),
//                    seller.getMobile()
//            );
//        }).collect(Collectors.toList());
//
//        if (status != null) {
//            history = history.stream()
//                    .filter(dto -> dto.getStatus().equalsIgnoreCase(status))
//                    .collect(Collectors.toList());
//        }
//
//        return ResponseEntity.ok(history);
//    }
//
//    @GetMapping("/seller-auction-history")
//    public ResponseEntity<List<SellerAuctionHistoryDto>> getSellerAuctionHistoryFromToken(
//            @RequestHeader("Authorization") String authHeader) {
//
//        String token = authHeader.substring(7);
//        String username = jwtUtil.getUsernameFromJwt(token);
//
//        User seller = userRepository.findByUsername(username)
//                .orElseThrow(() -> new RuntimeException("Seller not found"));
//
//        Long sellerId = seller.getId();
//        LocalDateTime now = LocalDateTime.now();
//
//        List<Product> products = productRepository.findBySellerId(sellerId);
//
//        List<SellerAuctionHistoryDto> result = products.stream().map(product -> {
//            String bidStatus;
//            String buyerName = "--";
//            String buyerMobile = "--";
//            String remainingTime = "--";
//            Double currentBidPrice = 0.0;
//            Double finalPrice = 0.0;
//
//            if (now.isBefore(product.getAuctionStart())) {
//                bidStatus = "UPCOMING";
//            } else if (now.isAfter(product.getAuctionEnd())) {
//                bidStatus = "EXPIRED";
//                remainingTime = "00:00:00";
//                Bid winner = bidRepository.findTopByProductIdOrderByBidPriceDesc(product.getId());
//                if (winner != null && winner.getUser() != null) {
//                    buyerName = winner.getUser().getFullname();
//                    buyerMobile = winner.getUser().getMobile();
//                    currentBidPrice = winner.getBidPrice();
//                    finalPrice = product.getFinalPrice() != null ? product.getFinalPrice() : currentBidPrice;
//                } else {
//                    finalPrice = product.getFinalPrice() != null ? product.getFinalPrice() : 0.0;
//                }
//            } else {
//                bidStatus = "LIVE";
//                Duration dur = Duration.between(now, product.getAuctionEnd());
//                remainingTime = String.format("%02d:%02d:%02d",
//                        dur.toHours(), dur.toMinutes() % 60, dur.getSeconds() % 60);
//                Bid highest = bidRepository.findTopByProductIdOrderByBidPriceDesc(product.getId());
//                if (highest != null) {
//                    currentBidPrice = highest.getBidPrice();
//                    finalPrice = currentBidPrice;
//                }
//                buyerName = "ONHOLD";
//                buyerMobile = "ONHOLD";
//            }
//
//            long totalBids = bidRepository.countByProductId(product.getId());
//
//            return new SellerAuctionHistoryDto(
//                    product.getId(),
//                    product.getName(),
//                    product.getBasePrice(),
//                    "/uploads/" + product.getImage(),
//                    product.getCategory(),
//                    totalBids,
//                    finalPrice,
//                    bidStatus,
//                    buyerName,
//                    buyerMobile,
//                    remainingTime,
//                    currentBidPrice
//            );
//        }).toList();
//
//        return ResponseEntity.ok(result);
//    }
//}
    @GetMapping("/bid-history")

    public ResponseEntity<List<BidHistoryDto>> getUserBidHistory(

            @RequestHeader("Authorization") String authHeader,

            @RequestParam(required = false) String status) {

        

        String token = authHeader.substring(7);

        String username = jwtUtil.getUsernameFromJwt(token);

        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {

            return ResponseEntity.badRequest().build();

        }



        List<Bid> userBids = bidRepository.findByUserId(user.getId());



        Map<Long, Bid> highestBidsPerProduct = userBids.stream()

            .collect(Collectors.toMap(

                bid -> bid.getProduct().getId(),

                bid -> bid,

                (existing, replacement) -> existing.getBidPrice() > replacement.getBidPrice() ? existing : replacement

            ));



        LocalDateTime now = LocalDateTime.now();



        List<BidHistoryDto> history = highestBidsPerProduct.values().stream().map(bid -> {

            Product product = bid.getProduct();

            User seller = product.getSeller();

            

            // Get the current winning bid (not just the user's highest)

            Bid currentWinningBid = bidRepository.findTopByProductIdOrderByBidPriceDesc(product.getId());

            

            boolean isWinner = currentWinningBid != null && 

                             currentWinningBid.getUser().getId().equals(user.getId()) && 

                             now.isAfter(product.getAuctionEnd());



            String bidStatus = now.isAfter(product.getAuctionEnd()) ? "EXPIRED" : "LIVE";

            String winStatus = isWinner ? "WON" : 

                             now.isAfter(product.getAuctionEnd()) ? "LOST" : "ONHOLD";



            Double finalPrice = product.getFinalPrice();

            if (finalPrice == null && now.isAfter(product.getAuctionEnd())) {

                finalPrice = currentWinningBid != null ? currentWinningBid.getBidPrice() : product.getBasePrice();

            }



            String remainingTime = calculateRemainingTime(product.getAuctionEnd());



            return new BidHistoryDto(

                    product.getId(),

                    product.getName(),

                    "/uploads/" + product.getImage(),

                    product.getCategory(),

                    product.getBasePrice(),

                    bid.getBidPrice(),

                    finalPrice,

                    remainingTime,

                    winStatus,

                    bidStatus,

                    currentWinningBid != null ? currentWinningBid.getBidPrice() : product.getBasePrice(),

                    seller.getUsername(),

                    seller.getMobile()

            );

        }).collect(Collectors.toList());



        if (status != null) {

            history = history.stream()

                    .filter(dto -> dto.getStatus().equalsIgnoreCase(status))

                    .collect(Collectors.toList());

        }



        return ResponseEntity.ok(history);

    }



    

    @MessageMapping("/bid/status")

    public void handleBidStatusUpdate(@Payload BidStatusUpdateRequest request) {

        Product product = productRepository.findById(request.getProductId()).orElse(null);

        if (product == null) return;



        User user = userRepository.findById(request.getUserId()).orElse(null);

        if (user == null) return;



        // Broadcast to the specific user

        messagingTemplate.convertAndSendToUser(

            user.getUsername(),

            "/queue/bid-status",

            Map.of(

                "productId", product.getId(),

                "status", request.getStatus(),

                "timestamp", LocalDateTime.now().toString()

            )

        );

    }

    @GetMapping("/seller-auction-history")

    public ResponseEntity<List<SellerAuctionHistoryDto>> getSellerAuctionHistoryFromToken(

            @RequestHeader("Authorization") String authHeader) {



        String token = authHeader.substring(7);

        String username = jwtUtil.getUsernameFromJwt(token);



        User seller = userRepository.findByUsername(username)

                .orElseThrow(() -> new RuntimeException("Seller not found"));



        Long sellerId = seller.getId();

        LocalDateTime now = LocalDateTime.now();



        List<Product> products = productRepository.findBySellerId(sellerId);



        List<SellerAuctionHistoryDto> result = products.stream().map(product -> {

            String bidStatus;

            String buyerName = "--";

            String buyerMobile = "--";

            String remainingTime = "--";

            Double currentBidPrice = 0.0;

            Double finalPrice = 0.0;



            if (now.isBefore(product.getAuctionStart())) {

                bidStatus = "UPCOMING";

            } else if (now.isAfter(product.getAuctionEnd())) {

                bidStatus = "EXPIRED";

                remainingTime = "00:00:00";

                Bid winner = bidRepository.findTopByProductIdOrderByBidPriceDesc(product.getId());

                if (winner != null && winner.getUser() != null) {

                    buyerName = winner.getUser().getFullname();

                    buyerMobile = winner.getUser().getMobile();

                    currentBidPrice = winner.getBidPrice();

                    finalPrice = product.getFinalPrice() != null ? product.getFinalPrice() : currentBidPrice;

                } else {

                    finalPrice = product.getFinalPrice() != null ? product.getFinalPrice() : 0.0;

                }

            } else {

                bidStatus = "LIVE";

                Duration dur = Duration.between(now, product.getAuctionEnd());

                remainingTime = String.format("%02d:%02d:%02d",

                        dur.toHours(), dur.toMinutes() % 60, dur.getSeconds() % 60);

                Bid highest = bidRepository.findTopByProductIdOrderByBidPriceDesc(product.getId());

                if (highest != null) {

                    currentBidPrice = highest.getBidPrice();

                    finalPrice = currentBidPrice;

                }

                buyerName = "ONHOLD";

                buyerMobile = "ONHOLD";

            }



            long totalBids = bidRepository.countByProductId(product.getId());



            return new SellerAuctionHistoryDto(

                    product.getId(),

                    product.getName(),

                    product.getBasePrice(),

                    "/uploads/" + product.getImage(),

                    product.getCategory(),

                    totalBids,

                    finalPrice,

                    bidStatus,

                    buyerName,

                    buyerMobile,

                    remainingTime,

                    currentBidPrice

            );

        }).toList();



        return ResponseEntity.ok(result);

    }
}