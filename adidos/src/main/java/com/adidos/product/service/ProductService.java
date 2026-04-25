package com.adidos.product.service;

import com.adidos.product.dto.*;
import com.adidos.product.entity.*;
import com.adidos.product.mapper.ProductMapper;
import com.adidos.product.repository.*;
import com.adidos.promotion.entity.Promotion;
import com.adidos.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;
    private final PromotionService promotionService;




    /**
     * Lấy CHI TIẾT sản phẩm kèm theo toàn bộ biến thể (Size, Color, Image)
     * Dùng cho trang Chi tiết sản phẩm (Detail)
     */
    @Transactional(readOnly = true)
    public ProductResponse getProductDetail(Long productId) {
        // Sử dụng hàm findByIdWithVariants (đã viết @Query JOIN FETCH) để tránh N+1 Query
        Product product = productRepository.findByIdWithVariants(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));



        ProductResponse response = ProductMapper.toProductResponse(product);
        applyPromotionData(response, product);
        return response;
    }




    //================= admin==============
    @Transactional
    public void saveProduct(ProductRequest request) {
        Product product;
        if (request.getId() != null) {
            product = productRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        } else {
            product = new Product();
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBrand(request.getBrand());
        product.setMaterial(request.getMaterial().trim().toLowerCase());
        product.setGender(request.getGender());
        product.setStatus(request.getStatus());

        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId()).orElse(null));
        }

        productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {

      Product product = productRepository.findById(id)
              .orElseThrow(()-> new RuntimeException("Không tìm thấy sản phẩm để xoá"));

      product.setStatus("INACTIVE");

      productRepository.save(product);

    }

    @Transactional
    public void saveVariant(VariantRequest request) {
        ProductVariant variant;
        if (request.getId() != null) {
            variant = productVariantRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));
        } else {
            // Kiểm tra xem tổ hợp Size/Color này đã tồn tại cho sản phẩm này chưa
            Optional<ProductVariant> existing = productVariantRepository
                    .findByProductIdAndSizeIdAndColorId(request.getProductId(), request.getSizeId(), request.getColorId());

            if (existing.isPresent()) {
                throw new RuntimeException("Biến thể với Size và Màu này đã tồn tại!");
            }
            variant = new ProductVariant();
            variant.setProduct(productRepository.findById(request.getProductId()).orElseThrow());
        }

        variant.setPrice(request.getPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setSize(sizeRepository.findById(request.getSizeId()).orElseThrow());
        variant.setColor(colorRepository.findById(request.getColorId()).orElseThrow());

        productVariantRepository.save(variant);
    }

    @Transactional
    public void deleteVariant(Long id) {

        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));

        productVariantRepository.deleteById(id);
    }

    private final ProductImageRepository productImageRepository;
    @Value("${upload.path}")
    private String uploadPath;

    @Transactional
    public void uploadImages(Long variantId, MultipartFile[] files, boolean applyToSameColor) {
        ProductVariant currentVariant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));

        List<ProductVariant> targetVariants = applyToSameColor && currentVariant.getColor() != null
                ? productVariantRepository.findByProductIdAndColorId(currentVariant.getProduct().getId(), currentVariant.getColor().getId())
                : List.of(currentVariant);

        try {
            // 1. Tạo thư mục uploads nếu chưa có
            String UPLOAD_DIR = "uploads/";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(UPLOAD_DIR);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            for (int i = 0; i < files.length; i++) {
                // 2. Code lưu file vật lý thật sự
                String fileName = System.currentTimeMillis() + "_" + files[i].getOriginalFilename();
                java.nio.file.Path filePath = uploadPath.resolve(fileName);
                java.nio.file.Files.copy(files[i].getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // 3. Lưu data vào DB
                for (ProductVariant variant : targetVariants) {
                    ProductImage image = new ProductImage();
                    image.setProductVariant(variant);
                    image.setImageUrl(fileName); // Chỉ lưu tên file

                    // Chỉ set ảnh đầu tiên làm chính nếu chưa có ảnh nào
                    boolean hasNoImages = productImageRepository.countByProductVariantId(variant.getId()) == 0;
                    image.setIsPrimary(i == 0 && hasNoImages);

                    productImageRepository.save(image);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu file: " + e.getMessage());
        }
    }

    @Transactional
    public void setPrimaryImage(Long imageId, Long variantId) {
        // Reset tất cả ảnh của variant này về false
        List<ProductImage> images = productImageRepository.findByProductVariantIdOrderBySortOrderAsc(variantId);
        images.forEach(img -> img.setIsPrimary(false));
        productImageRepository.saveAll(images);

        // Set ảnh được chọn làm true
        ProductImage primary = productImageRepository.findById(imageId).orElseThrow();
        primary.setIsPrimary(true);
        productImageRepository.save(primary);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        productImageRepository.deleteById(imageId);
    }

    /**
     * Lấy chi tiết biến thể dành riêng cho trang Quản lý hình ảnh
     */
    @Transactional(readOnly = true)
    public VariantDetailResponse getVariantDetail(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));

        // Map danh sách ảnh Entity sang ImageResponse DTO
        List<ImageResponse> images = variant.getImages().stream()
                .map(img -> ImageResponse.builder()
                        .id(img.getId())
                        .url(img.getImageUrl())
                        .isPrimary(img.getIsPrimary())
                        .build())
                .collect(Collectors.toList());

        return VariantDetailResponse.builder()
                .id(variant.getId())
                .productId(variant.getProduct().getId())
                .sizeName(variant.getSize().getSizeName())
                .colorName(variant.getColor().getColorName())
                .imageUrlsWithData(images)
                .build();
    }

    /**
     * Lấy tất cả sản phẩm đang bán (Status = ACTIVE)
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllActiveProducts() {
        List<Product> products = productRepository.findByStatus("ACTIVE");
        return products.stream()
                .map(p -> {
                    ProductResponse res = ProductMapper.toProductResponse(p);
                    applyPromotionData(res, p); // Nhồi thêm giá Khuyến mãi
                    return res;
                })
                .collect(Collectors.toList());
    }









    public List<Color> getAllColors() {
        return colorRepository.findAll();
    }

    public List<Size> getAllSizes() {
        return sizeRepository.findAll();
    }


    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategoryId(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String brand, String material) {
        List<Product> products = productRepository.findProductsByCategoryAndSub(categoryId);
        return products.stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()))
                // Lọc theo Brand (nếu có)
                .filter(p -> brand == null || brand.isEmpty() || brand.equalsIgnoreCase(p.getBrand()))
                // Lọc theo Material (nếu có)
                .filter(p -> material == null || material.isEmpty() || material.equalsIgnoreCase(p.getMaterial()))
                .map(p -> {
                    ProductResponse res = ProductMapper.toProductResponse(p);
                    applyPromotionData(res, p);
                    return res;
                })
                // Lọc theo khoảng giá (Lọc trên giá đã giảm)
                .filter(res -> minPrice == null || res.getDiscountedPrice().compareTo(minPrice) >= 0)
                .filter(res -> maxPrice == null || res.getDiscountedPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());
    }

    // Lấy danh sách thương hiệu không trùng lặp (Viết hoa toàn bộ cho đẹp: NIKE, ADIDAS)
    public List<String> getBrandsByCategory(Long categoryId) {
        return productRepository.findProductsByCategoryAndSub(categoryId).stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()) && p.getBrand() != null && !p.getBrand().trim().isEmpty())
                .map(p -> p.getBrand().trim().toUpperCase()) // Chuẩn hóa in hoa
                .distinct()
                .collect(Collectors.toList());
    }

    // Lấy danh sách chất liệu không trùng lặp (Viết hoa chữ cái đầu: Cotton, Polyester)
    public List<String> getMaterialsByCategory(Long categoryId) {
        return productRepository.findProductsByCategoryAndSub(categoryId).stream()
                .filter(p -> "ACTIVE".equalsIgnoreCase(p.getStatus()) && p.getMaterial() != null && !p.getMaterial().trim().isEmpty())
                .map(p -> {
                    String m = p.getMaterial().trim();
                    return m.substring(0, 1).toUpperCase() + m.substring(1).toLowerCase(); // Chuẩn hóa chữ đầu
                })
                .distinct()
                .collect(Collectors.toList());
    }



    private void applyPromotionData(ProductResponse response, Product product) {
        BigDecimal originalPrice = product.getVariants().isEmpty() ? BigDecimal.ZERO : product.getVariants().get(0).getPrice();
        Long categoryId = product.getCategory() != null ? product.getCategory().getId() : null;

        // 1. Lấy khuyến mãi để gắn tên
        Promotion promo = promotionService.getBestPromotionForCategory(categoryId);
        if (promo != null) {
            response.setPromotionName(promo.getPromotionName());

            response.setDiscountType(promo.getDiscountType());
            response.setPromotionDiscountValue(promo.getDiscountValue());
        }

        // 2. Tính giá giảm cho sản phẩm gốc
        BigDecimal discountedPrice = promotionService.calculateDiscountedPrice(categoryId, originalPrice);
        response.setOriginalPrice(originalPrice);
        response.setDiscountedPrice(discountedPrice);
        response.setHasPromotion(discountedPrice.compareTo(originalPrice) < 0);

        // 3. Tính giá giảm cho TỪNG BIẾN THỂ để gửi ra HTML
        if (response.getVariants() != null) {
            response.getVariants().forEach(variant -> {
                BigDecimal vDiscounted = promotionService.calculateDiscountedPrice(categoryId, variant.getPrice());
                variant.setDiscountedPrice(vDiscounted);
            });
        }

        // 4. Logic 7 ngày cho hàng mới
        response.setIsNew(product.getCreatedAt() != null && product.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusDays(7)));
    }

    public List<ProductResponse> getProductsForPromotion(Promotion promo) {
        if (promo == null || promo.getCategories().isEmpty()) return List.of();
        List<Long> catIds = promo.getCategories().stream().map(Category::getId).toList();
        return productRepository.findTop4ByCategoryIdInAndStatus(catIds, "ACTIVE")
                .stream().map(p -> {
                    ProductResponse res = ProductMapper.toProductResponse(p);
                    applyPromotionData(res, p);
                    return res;
                }).toList();
    }












}