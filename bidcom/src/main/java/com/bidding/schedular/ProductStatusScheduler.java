package com.bidding.schedular;



import com.bidding.dto.*;

import com.bidding.model.Bid;

import com.bidding.model.Product;

import com.bidding.model.User;

import com.bidding.repository.BidRepository;

import com.bidding.repository.ProductRepository;

import com.bidding.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.mail.SimpleMailMessage;

import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Component;



import java.time.Duration;

import java.time.LocalDateTime;

import java.util.List;

import java.util.Map;

import java.util.stream.Collectors;



@Component

public class ProductStatusScheduler {



    private final ProductRepository productRepository;

    private final BidRepository bidRepository;

    private final UserRepository userRepository;

    private final JavaMailSender mailSender;

    private final SimpMessagingTemplate messagingTemplate;



    public ProductStatusScheduler(ProductRepository productRepository,

                                BidRepository bidRepository,

                                UserRepository userRepository,

                                JavaMailSender mailSender,

                                SimpMessagingTemplate messagingTemplate) {

        this.productRepository = productRepository;

        this.bidRepository = bidRepository;

        this.userRepository = userRepository;

        this.mailSender = mailSender;

        this.messagingTemplate = messagingTemplate;

    }



    @Scheduled(fixedRate = 10000) // every 10 seconds

    @Transactional

    public void updateProductStatuses() {

        List<Product> products = productRepository.findAll();

        LocalDateTime now = LocalDateTime.now();



        for (Product product : products) {

            String currentStatus = product.getStatus();

            String newStatus = calculateProductStatus(product, now);



            if (!currentStatus.equals(newStatus)) {

                updateProductStatus(product, newStatus);

                broadcastStatusChange(product, currentStatus, newStatus);

            }



            if ("ENDED".equals(product.getStatus()) && product.getFinalPrice() == null) {

                finalizeAuction(product);

            }

        }

    }



    private String calculateProductStatus(Product product, LocalDateTime now) {

        if (now.isBefore(product.getAuctionStart())) {

            return "UPCOMING";

        } else if (now.isAfter(product.getAuctionEnd())) {

            return "ENDED";

        } else {

            return "LIVE";

        }

    }



    private void updateProductStatus(Product product, String newStatus) {

        product.setStatus(newStatus);

        productRepository.save(product);

        System.out.println("🔄 Product ID " + product.getId() + " status changed to " + newStatus);

    }



    private void broadcastStatusChange(Product product, String oldStatus, String newStatus) {

        String remainingTime = calculateRemainingTime(product.getAuctionEnd());



        ProductFullResponseDto productDto = new ProductFullResponseDto(

                product.getId(),

                product.getName(),

                product.getDescription(),

                product.getBasePrice(),

                "/uploads/" + product.getImage(),

                product.getSeller().getFullname(),

                product.getSeller().getMobile(),

                product.getCategory(),

                product.getAuctionStart().toString(),

                product.getAuctionEnd().toString(),

                remainingTime,

                newStatus

        );



        if ("UPCOMING".equals(oldStatus) && "LIVE".equals(newStatus)) {

            messagingTemplate.convertAndSend("/topic/products/live", productDto);

        }



        messagingTemplate.convertAndSend("/topic/products/" + product.getId() + "/status", 

            Map.of(

                "productId", product.getId(),

                "oldStatus", oldStatus,

                "newStatus", newStatus,

                "remainingTime", remainingTime

            )

        );

        

        messagingTemplate.convertAndSend("/topic/products/status-updates", 

            Map.of(

                "productId", product.getId(),

                "name", product.getName(),

                "image", "/uploads/" + product.getImage(),

                "oldStatus", oldStatus,

                "newStatus", newStatus

            )

        );

    }



    private void finalizeAuction(Product product) {

        Bid winningBid = bidRepository.findTopByProductIdOrderByBidPriceDesc(product.getId());

        

        if (winningBid != null) {

            product.setFinalPrice(winningBid.getBidPrice());

            productRepository.save(product);



            // Get all bids for the product

            List<Bid> allBids = bidRepository.findByProductId(product.getId());

            

            // Notify all participants

            broadcastAuctionResult(product, winningBid, allBids);

            

            // Notify winner specifically

            notifyWinner(product, winningBid);

            

            // Notify losers

            notifyOtherBidders(product, winningBid, allBids);

        } else {

            broadcastNoBidsResult(product);

        }

    }



    private void broadcastAuctionResult(Product product, Bid winningBid, List<Bid> allBids) {

        // General result notification

        messagingTemplate.convertAndSend("/topic/products/" + product.getId() + "/result", 

            Map.of(

                "productId", product.getId(),

                "status", "ENDED",

                "winner", winningBid.getUser().getUsername(),

                "winningPrice", winningBid.getBidPrice(),

                "productName", product.getName()

            )

        );

        

        // Specific win/lose notifications

        allBids.forEach(bid -> {

            String resultStatus = bid.getId().equals(winningBid.getId()) ? "WON" : "LOST";

            

            messagingTemplate.convertAndSendToUser(

                bid.getUser().getUsername(),

                "/queue/bid-results",

                Map.of(

                    "productId", product.getId(),

                    "status", resultStatus,

                    "productName", product.getName(),

                    "finalPrice", winningBid.getBidPrice(),

                    "yourBid", bid.getBidPrice()

                )

            );

        });

    }



    private void notifyWinner(Product product, Bid winningBid) {

        try {

            User winner = winningBid.getUser();

            

            // WebSocket notification

            messagingTemplate.convertAndSendToUser(

                winner.getUsername(),

                "/queue/bid-wins",

                Map.of(

                    "productId", product.getId(),

                    "productName", product.getName(),

                    "winningPrice", winningBid.getBidPrice(),

                    "timestamp", LocalDateTime.now().toString()

                )

            );



            // Email notification

            sendWinnerEmail(product, winningBid);

        } catch (Exception e) {

            System.err.println("Failed to notify winner: " + e.getMessage());

        }

    }



    private void notifyOtherBidders(Product product, Bid winningBid, List<Bid> allBids) {

        allBids.stream()

            .filter(bid -> !bid.getId().equals(winningBid.getId()))

            .forEach(bid -> {

                try {

                    messagingTemplate.convertAndSendToUser(

                        bid.getUser().getUsername(),

                        "/queue/bid-results",

                        Map.of(

                            "productId", product.getId(),

                            "status", "LOST",

                            "productName", product.getName(),

                            "winningPrice", winningBid.getBidPrice(),

                            "yourBid", bid.getBidPrice()

                        )

                    );

                } catch (Exception e) {

                    System.err.println("Failed to notify bidder: " + e.getMessage());

                }

            });

    }



    private void sendWinnerEmail(Product product, Bid winningBid) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(winningBid.getUser().getEmail());

        message.setSubject("🎉 You've won the auction for " + product.getName());

        message.setText(createWinEmailContent(product, winningBid));

        mailSender.send(message);

    }



    private void broadcastNoBidsResult(Product product) {

        messagingTemplate.convertAndSend("/topic/products/" + product.getId() + "/result", 

            Map.of(

                "productId", product.getId(),

                "status", "ENDED_NO_BIDS",

                "message", "This auction ended with no bids"

            )

        );

    }



    private String createWinEmailContent(Product product, Bid winningBid) {

        return String.format(

            "Congratulations %s!\n\n" +

            "You have won the auction for:\n" +

            "Product: %s\n" +

            "Your Winning Bid: ₹%.2f\n\n" +

            "Next Steps:\n" +

            "1. Log in to your account\n" +

            "2. Go to 'My Won Auctions'\n" +

            "3. Complete the purchase process\n\n" +

            "Thanks for using our platform!\n" +

            "The Auction Team",

            winningBid.getUser().getFullname(),

            product.getName(),

            winningBid.getBidPrice()

        );

    }



    private String calculateRemainingTime(LocalDateTime endTime) {

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(endTime)) return "00:00:00";

        

        Duration duration = Duration.between(now, endTime);

        return String.format("%02d:%02d:%02d",

            duration.toHours(),

            duration.toMinutes() % 60,

            duration.getSeconds() % 60);

    }

}