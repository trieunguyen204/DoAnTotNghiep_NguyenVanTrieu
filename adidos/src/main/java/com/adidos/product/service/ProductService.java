package com.adidos.product.service;

import com.adidos.product.dto.*;
import com.adidos.product.entity.Product;
import com.adidos.product.entity.ProductImage;
import com.adidos.product.entity.ProductVariant;
import com.adidos.product.mapper.ProductMapper;
import com.adidos.product.repository.*;
import com.adidos.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;


import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import com.adidos.promotion.service.PromotionService;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
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

        // Mapper sẽ tự động lo việc bóc tách Variant, Ảnh và tìm giá thấp nhất
        return ProductMapper.toProductResponse(product);
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
        product.setMaterial(request.getMaterial());
        product.setGender(request.getGender());
        product.setStatus(request.getStatus());

        if (request.getCategoryId() != null) {
            product.setCategory(categoryRepository.findById(request.getCategoryId()).orElse(null));
        }

        productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
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
        productVariantRepository.deleteById(id);
    }

    private final ProductImageRepository productImageRepository;
    @Value("${upload.path}")
    private String uploadPath;

    @Transactional
    public void uploadImages(Long variantId, MultipartFile[] files) throws IOException {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                // Tạo tên file duy nhất để tránh trùng lặp
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get(uploadPath + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                // Lưu vào DB
                ProductImage image = ProductImage.builder()
                        .productVariant(variant)
                        .imageUrl("/uploads/" + fileName)
                        .isPrimary(false)
                        .sortOrder(0)
                        .build();
                productImageRepository.save(image);
            }
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

    /**
     * Lấy sản phẩm theo Danh mục
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream()
                .map(p -> {
                    ProductResponse res = ProductMapper.toProductResponse(p);
                    applyPromotionData(res, p);
                    return res;
                })
                .collect(Collectors.toList());
    }



    /**
     * HÀM PHỤ TRỢ: Tính toán và nhồi dữ liệu Promotion vào ProductResponse
     */
    private void applyPromotionData(ProductResponse response, Product product) {
        // Lấy giá gốc từ Variant đầu tiên (hoặc 0 nếu chưa có Variant)
        BigDecimal originalPrice = product.getVariants().isEmpty() ? BigDecimal.ZERO : product.getVariants().get(0).getPrice();

        Long categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
        BigDecimal discountedPrice = originalPrice;

        // Tính giá sau khuyến mãi
        if (categoryId != null && originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            discountedPrice = promotionService.calculateDiscountedPrice(categoryId, originalPrice);
        }

        response.setOriginalPrice(originalPrice);
        response.setDiscountedPrice(discountedPrice);
        response.setHasPromotion(discountedPrice.compareTo(originalPrice) < 0);
    }
}