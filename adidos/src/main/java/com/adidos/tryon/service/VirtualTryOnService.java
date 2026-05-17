package com.adidos.tryon.service;

import com.adidos.product.entity.ProductVariant;
import com.adidos.tryon.dto.TryOnColorResult;
import com.adidos.tryon.dto.TryOnResponse;
import com.adidos.tryon.entity.TryOnStatus;
import com.adidos.tryon.entity.VirtualTryOn;
import com.adidos.tryon.repository.TryOnProductVariantRepository;
import com.adidos.tryon.repository.VirtualTryOnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VirtualTryOnService {

    private final TryOnProductVariantRepository productVariantRepository;
    private final VirtualTryOnRepository virtualTryOnRepository;
    private final TryOnImageResolverService imageResolverService;
    private final TryOnFileStorageService fileStorageService;
    private final TryOnAiService aiService;

    public List<TryOnResponse> getHistory(Long productId, String sessionId) {
        return virtualTryOnRepository
                .findTop20ByProductIdAndSessionIdOrderByCreatedAtDesc(productId, sessionId)
                .stream()
                .map(job -> toResponse(job, null))
                .toList();
    }

    @Transactional
    public TryOnResponse createTryOn(Long productId, Long variantId, MultipartFile personImage, String sessionId) {

        ProductVariant selectedVariant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể sản phẩm"));

        if (selectedVariant.getProduct() == null || !selectedVariant.getProduct().getId().equals(productId)) {
            throw new IllegalArgumentException("Biến thể không thuộc sản phẩm hiện tại");
        }

        String personImageUrl = fileStorageService.savePersonImage(personImage);

        List<ProductVariant> allVariants = productVariantRepository.findAllByProductId(productId);

        if (allVariants == null || allVariants.isEmpty()) {
            throw new IllegalArgumentException("Sản phẩm chưa có biến thể để thử đồ");
        }

        String selectedSizeName = getSizeName(selectedVariant);

        List<ProductVariant> representativeVariants =
                chooseOneVariantPerColorPreferSelectedSize(allVariants, selectedSizeName);

        List<TryOnColorResult> colorResults = new ArrayList<>();

        VirtualTryOn firstSavedJob = null;

        for (ProductVariant variant : representativeVariants) {

            String colorName = getColorName(variant);
            String sizeName = getSizeName(variant);

            VirtualTryOn job = null;

            try {
                String garmentImageUrl = imageResolverService.resolveGarmentImageUrl(productId, variant);

                job = VirtualTryOn.builder()
                        .product(variant.getProduct())
                        .productVariant(variant)
                        .personImageUrl(personImageUrl)
                        .garmentImageUrl(garmentImageUrl)
                        .status(TryOnStatus.PROCESSING)
                        .sessionId(sessionId)
                        .build();

                job = virtualTryOnRepository.save(job);

                if (firstSavedJob == null) {
                    firstSavedJob = job;
                }

                String resultImageUrl = aiService.generateTryOn(personImageUrl, garmentImageUrl);

                job.setResultImageUrl(resultImageUrl);
                job.setStatus(TryOnStatus.SUCCESS);
                job = virtualTryOnRepository.save(job);

                colorResults.add(
                        TryOnColorResult.builder()
                                .variantId(variant.getId())
                                .colorName(colorName)
                                .sizeName(sizeName)
                                .garmentImageUrl(garmentImageUrl)
                                .resultImageUrl(resultImageUrl)
                                .status(TryOnStatus.SUCCESS)
                                .build()
                );

            } catch (Exception e) {

                if (job != null) {
                    job.setStatus(TryOnStatus.FAILED);
                    job.setErrorMessage(e.getMessage());
                    job = virtualTryOnRepository.save(job);

                    if (firstSavedJob == null) {
                        firstSavedJob = job;
                    }
                }

                colorResults.add(
                        TryOnColorResult.builder()
                                .variantId(variant.getId())
                                .colorName(colorName)
                                .sizeName(sizeName)
                                .status(TryOnStatus.FAILED)
                                .errorMessage(e.getMessage())
                                .build()
                );
            }
        }

        if (firstSavedJob == null) {
            throw new IllegalArgumentException("Không tạo được kết quả thử đồ");
        }

        return toResponse(firstSavedJob, colorResults);
    }

    @Transactional(readOnly = true)
    public TryOnResponse getTryOn(Long id) {
        VirtualTryOn job = virtualTryOnRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch sử thử đồ"));

        return toResponse(job, null);
    }

    private List<ProductVariant> chooseOneVariantPerColorPreferSelectedSize(
            List<ProductVariant> variants,
            String selectedSizeName
    ) {
        Map<String, ProductVariant> byColor = new LinkedHashMap<>();

        for (ProductVariant variant : variants) {

            if (!isActive(variant)) {
                continue;
            }

            String colorName = getColorName(variant);

            if (colorName == null || colorName.isBlank()) {
                colorName = "Màu khác";
            }

            ProductVariant current = byColor.get(colorName);

            if (current == null) {
                byColor.put(colorName, variant);
                continue;
            }

            String variantSize = getSizeName(variant);
            String currentSize = getSizeName(current);

            boolean variantMatchesSelectedSize =
                    selectedSizeName != null && selectedSizeName.equalsIgnoreCase(variantSize);

            boolean currentMatchesSelectedSize =
                    selectedSizeName != null && selectedSizeName.equalsIgnoreCase(currentSize);

            if (variantMatchesSelectedSize && !currentMatchesSelectedSize) {
                byColor.put(colorName, variant);
            }
        }

        return new ArrayList<>(byColor.values());
    }

    private boolean isActive(ProductVariant variant) {
        return variant != null
                && variant.getStatus() != null
                && "ACTIVE".equalsIgnoreCase(variant.getStatus());
    }

    private String getColorName(ProductVariant variant) {
        if (variant == null || variant.getColor() == null) {
            return null;
        }

        return variant.getColor().getColorName();
    }

    private String getSizeName(ProductVariant variant) {
        if (variant == null || variant.getSize() == null) {
            return null;
        }

        return variant.getSize().getSizeName();
    }

    private TryOnResponse toResponse(VirtualTryOn job, List<TryOnColorResult> colorResults) {
        ProductVariant variant = job.getProductVariant();

        return TryOnResponse.builder()
                .id(job.getId())
                .productId(job.getProduct().getId())
                .variantId(variant.getId())
                .colorName(getColorName(variant))
                .sizeName(getSizeName(variant))
                .personImageUrl(job.getPersonImageUrl())
                .garmentImageUrl(job.getGarmentImageUrl())
                .resultImageUrl(job.getResultImageUrl())
                .status(job.getStatus())
                .errorMessage(job.getErrorMessage())
                .colorResults(colorResults)
                .build();
    }
}