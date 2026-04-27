package com.adidos.voucher.service;

import com.adidos.voucher.entity.Voucher;
import com.adidos.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VoucherService {

    private final VoucherRepository voucherRepository;

    @Transactional
    public List<Voucher> getAllVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll();

        vouchers.forEach(this::refreshVoucherStatus);

        return voucherRepository.saveAll(vouchers);
    }

    @Transactional
    public Voucher saveVoucher(Voucher voucher) {
        voucher.setCode(voucher.getCode().toUpperCase().trim());

        if (voucher.getUsedCount() == null) {
            voucher.setUsedCount(0);
        }

        refreshVoucherStatus(voucher);

        return voucherRepository.save(voucher);
    }

    public BigDecimal calculateDiscount(String code, BigDecimal orderTotal) {
        if (code == null || code.isBlank()) {
            return BigDecimal.ZERO;
        }

        Voucher voucher = voucherRepository.findByCode(code.toUpperCase().trim())
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));

        refreshVoucherStatus(voucher);
        voucherRepository.save(voucher);

        if (!"ACTIVE".equalsIgnoreCase(voucher.getStatus())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn hoặc không còn hoạt động");
        }

        if (voucher.getMinOrderValue() != null
                && orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new RuntimeException("Đơn hàng phải từ "
                    + voucher.getMinOrderValue()
                    + "đ để sử dụng mã này");
        }

        BigDecimal discount = BigDecimal.ZERO;

        if ("FIXED".equalsIgnoreCase(voucher.getDiscountType())) {
            discount = voucher.getDiscountValue();

        } else if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
            discount = orderTotal
                    .multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));

            if (voucher.getMaxDiscountValue() != null
                    && voucher.getMaxDiscountValue().compareTo(BigDecimal.ZERO) > 0
                    && discount.compareTo(voucher.getMaxDiscountValue()) > 0) {
                discount = voucher.getMaxDiscountValue();
            }
        }

        return discount.compareTo(orderTotal) > 0 ? orderTotal : discount;
    }

    @Transactional
    public Voucher getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Voucher"));

        refreshVoucherStatus(voucher);

        return voucherRepository.save(voucher);
    }

    @Transactional
    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }

    @Transactional
    public List<Voucher> getAvailableVouchers() {
        List<Voucher> vouchers = voucherRepository.findAll();

        vouchers.forEach(this::refreshVoucherStatus);
        voucherRepository.saveAll(vouchers);

        return vouchers.stream()
                .filter(v -> "ACTIVE".equalsIgnoreCase(v.getStatus()))
                .toList();
    }

    private void refreshVoucherStatus(Voucher voucher) {
        LocalDateTime now = LocalDateTime.now();

        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            voucher.setStatus("EXPIRED");
            return;
        }

        if (voucher.getUsageLimit() != null
                && voucher.getUsedCount() != null
                && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            voucher.setStatus("EXPIRED");
            return;
        }

        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            voucher.setStatus("INACTIVE");
            return;
        }

        voucher.setStatus("ACTIVE");
    }
}