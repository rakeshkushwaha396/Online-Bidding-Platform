package com.bidding.controller;

import com.bidding.dto.ProductDetailsDto;
import com.bidding.dto.ProductFullResponseDto;
import com.bidding.dto.ProductResponseDto;
import com.bidding.dto.ProductResponseWithSellerDto;
import com.bidding.model.Product;
import com.bidding.model.User;
import com.bidding.repository.BidRepository;
import com.bidding.repository.ProductRepository;
import com.bidding.repository.UserRepository;
import com.bidding.security.JwtUtil;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/products")
@AllArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
	private final JwtUtil jwtUtil;
	private final SimpMessagingTemplate messagingTemplate;


    
    
    

	@GetMapping("/all")
    public List<ProductFullResponseDto> getAllProducts() {
        List<Product> products = productRepository.findAllActiveProducts();
        LocalDateTime now = LocalDateTime.now();

        return products.stream().map(product -> {
            LocalDateTime auctionEnd = product.getAuctionEnd();
            Duration duration = Duration.between(now, auctionEnd);
            String remainingTime = (now.isAfter(auctionEnd)) ? "00:00:00" :
                    String.format("%02d:%02d:%02d",
                            duration.toHours(),
                            duration.toMinutes() % 60,
                            duration.getSeconds() % 60);

            String status;
            if (now.isBefore(product.getAuctionStart())) {
                status = "UPCOMING";
            } else if (now.isAfter(product.getAuctionEnd())) {
                status = "ENDED";
            } else {
                status = "LIVE";
            }

            return new ProductFullResponseDto(
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
                    status
            );
        }).toList();
    }



  
    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/upload")

    public ResponseEntity<?> uploadProduct(

            @RequestHeader("Authorization") String authHeader,

            @RequestParam("category") String category,

            @RequestParam("name") String name,

            @RequestParam("description") String description,

            @RequestParam("basePrice") Double basePrice,

            @RequestParam("auctionStart") String auctionStartStr,

            @RequestParam("auctionEnd") String auctionEndStr,

            @RequestParam("image") MultipartFile imageFile

    ) {

        try {

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)

                        .body("Missing or invalid token");

            }

            String token = authHeader.substring(7);

            String username = jwtUtil.getUsernameFromJwt(token);

            User user = userRepository.findByUsername(username)

                    .orElseThrow(() -> new RuntimeException("User not found"));

            ResponseEntity<?> imageValidation = validateImageFile(imageFile, "Product image");

            if (imageValidation != null) {

                return imageValidation;

            }
            LocalDateTime auctionStart = LocalDateTime.parse(auctionStartStr);

            LocalDateTime auctionEnd = LocalDateTime.parse(auctionEndStr);

            

            if (auctionStart.isAfter(auctionEnd)) {

                return ResponseEntity.badRequest()

                        .body("Auction start must be before auction end.");

            }

            if (auctionStart.isBefore(LocalDateTime.now())) {

                return ResponseEntity.badRequest()

                        .body("Auction start cannot be in the past.");

            }




            String filename = saveFile(imageFile);



            Product product = new Product();

            product.setCategory(category);

            product.setSeller(user);

            product.setName(name);

            product.setDescription(description);

            product.setImage(filename);

            product.setBasePrice(basePrice);

            product.setAuctionStart(auctionStart);

            product.setAuctionEnd(auctionEnd);

            product.setStatus("UPCOMING");



            productRepository.save(product);

            


            broadcastNewProduct(product, user);



            return ResponseEntity.ok("Product uploaded successfully!");



        } catch (IOException e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body("Failed to upload product image: " + e.getMessage());

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

                    .body("Error: " + e.getMessage());

        }

    }



    private void broadcastNewProduct(Product product, User seller) {

        LocalDateTime now = LocalDateTime.now();

        String remainingTime;

        String status = "UPCOMING";

        

        if (now.isBefore(product.getAuctionStart())) {

            Duration duration = Duration.between(now, product.getAuctionStart());

            remainingTime = String.format("%02d:%02d:%02d",

                    duration.toHours(),

                    duration.toMinutes() % 60,

                    duration.getSeconds() % 60);

        } else {

            remainingTime = "00:00:00";

        }



        ProductFullResponseDto productDto = new ProductFullResponseDto(

                product.getId(),

                product.getName(),

                product.getDescription(),

                product.getBasePrice(),

                "/uploads/" + product.getImage(),

                seller.getFullname(),

                seller.getMobile(),

                product.getCategory(),

                product.getAuctionStart().toString(),

                product.getAuctionEnd().toString(),

                remainingTime,

                status

        );




        messagingTemplate.convertAndSend("/topic/products/new", productDto);

        


        if (!"UPCOMING".equals(product.getStatus())) {

            messagingTemplate.convertAndSend("/topic/products/live", productDto);

        }

        


        messagingTemplate.convertAndSendToUser(

                seller.getUsername(),

                "/queue/products/confirmation",

                Map.of(

                    "message", "Product uploaded successfully",

                    "productId", product.getId(),

                    "productName", product.getName()

                )

        );

    }

    private ResponseEntity<?> validateImageFile(MultipartFile file, String fieldName) {

        if (file == null || file.isEmpty()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)

                    .body(fieldName + " is required");

        }

        String contentType = file.getContentType();

        if (contentType == null || 

            !(contentType.equalsIgnoreCase("image/jpeg") || 

              contentType.equalsIgnoreCase("image/jpg") || 

              contentType.equalsIgnoreCase("image/png") ||

              contentType.equalsIgnoreCase("image/webp"))) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)

                    .body(fieldName + " must be in JPG, JPEG, PNG or WEBP format");

        }




        long maxFileSize = 5 * 1024 * 1024;

        if (file.getSize() > maxFileSize) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)

                    .body(fieldName + " size must be less than 5MB");

        }



        return null; 

    }
    @GetMapping("/live")
    public List<ProductResponseDto> getLiveProducts(@RequestParam(required = false) String category) {
        List<Product> products;

        if (category != null) {
            products = productRepository.findByStatusAndCategory("LIVE", category);
        } else {
            products = productRepository.findByStatus("LIVE");
        }

        return products.stream().map(product -> new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBasePrice(),
                "/uploads/" + product.getImage()
        )).toList();
    }


    private String saveFile(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();

        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs(); 
        }

        File dest = new File(dir, filename);
        file.transferTo(dest);

        System.out.println("Uploading to: " + dest.getAbsolutePath());

        return filename;
        
    }
    

    
    @GetMapping("/live-with-seller")
    public List<ProductResponseWithSellerDto> getLiveProductsWithSeller() {
        List<Product> products = productRepository.findByStatus("LIVE");

        return products.stream().map(product -> {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = product.getAuctionStart();
            LocalDateTime end = product.getAuctionEnd();

            String remaining;
            if (now.isAfter(end)) {
                remaining = "00:00:00";
            } else {
                Duration duration = Duration.between(now, end);
                long hours = duration.toHours();
                long minutes = duration.toMinutes() % 60;
                long seconds = duration.getSeconds() % 60;
                remaining = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
            
            String status;
            if (now.isBefore(start)) {
                status = "UPCOMING";
            } else if (now.isAfter(end)) {
                status = "ENDED";
            } else {
                status = "LIVE";
            }

            return new ProductResponseWithSellerDto(
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
                    remaining,
                    status
            );
        }).toList();
    }
    
    @GetMapping("/details/{productId}")
    public ResponseEntity<?> getProductDetails(@PathVariable Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid product ID");
        }

        Product product = optionalProduct.get();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = product.getAuctionStart();
        LocalDateTime end = product.getAuctionEnd();
        String status;
        if (now.isBefore(start)) {
            status = "UPCOMING";
        } else if (now.isAfter(end)) {
            status = "ENDED";
        } else {
            status = "LIVE";
        }

        String remaining;
        if (now.isAfter(end)) {
            remaining = "00:00:00";
        } else {
            Duration duration = Duration.between(now, end);
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;
            long seconds = duration.getSeconds() % 60;
            remaining = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }

        // Fetch current bid & count
        Double currentBid = bidRepository.findHighestBidAmount(productId);
        if (currentBid == null) {
            currentBid = product.getBasePrice(); 
        }

        Long totalBids = bidRepository.countByProductId(productId);

        ProductDetailsDto dto = new ProductDetailsDto(
                product.getId(),
                product.getName(),
                product.getCategory(),
                product.getDescription(),
                product.getBasePrice(),
                currentBid,
                totalBids,
                remaining,
                status,
                product.getSeller().getFullname(),
                product.getSeller().getMobile(),
                "/uploads/" + product.getImage()
        );

        return ResponseEntity.ok(dto);
    }


}
