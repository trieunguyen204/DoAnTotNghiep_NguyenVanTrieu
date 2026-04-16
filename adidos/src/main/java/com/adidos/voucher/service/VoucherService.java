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
public class VoucherService {

    private final VoucherRepository voucherRepository;

    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Transactional
    public void saveVoucher(Voucher voucher) {
        voucher.setCode(voucher.getCode().toUpperCase().trim());
        if (voucher.getStatus() == null) voucher.setStatus("ACTIVE");
        voucherRepository.save(voucher);
    }

    /**
     * Logic tính toán số tiền giảm giá thực tế
     */
    public BigDecimal calculateDiscount(String code, BigDecimal orderTotal) {
        if (code == null || code.isEmpty()) return BigDecimal.ZERO;

        Voucher voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không hợp lệ"));

        // 1. Kiểm tra trạng thái
        if (!"ACTIVE".equals(voucher.getStatus())) {
            throw new RuntimeException("Mã giảm giá đã bị khóa hoặc không còn hoạt động");
        }

        // 2. Kiểm tra số lượng
        if (voucher.getQuantity() <= 0) {
            throw new RuntimeException("Mã giảm giá này đã hết lượt sử dụng");
        }

        // 3. Kiểm tra thời hạn
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            throw new RuntimeException("Chương trình giảm giá chưa bắt đầu");
        }
        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            throw new RuntimeException("Mã giảm giá đã hết hạn");
        }

        // 4. Kiểm tra giá trị đơn hàng tối thiểu
        if (voucher.getMinOrderValue() != null && orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new RuntimeException("Đơn hàng phải từ " + voucher.getMinOrderValue() + "đ để sử dụng mã này");
        }

        // 5. Tính toán số tiền giảm
        BigDecimal discount = BigDecimal.ZERO;
        if ("FIXED".equalsIgnoreCase(voucher.getDiscountType())) {
            discount = voucher.getDiscountValue();
        } else if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
            // Giảm theo % (Ví dụ: 10% của 1,000,000 là 100,000)
            discount = orderTotal.multiply(voucher.getDiscountValue().divide(new BigDecimal(100)));
        }

        // Đảm bảo tiền giảm không vượt quá giá trị đơn hàng
        return discount.compareTo(orderTotal) > 0 ? orderTotal : discount;
    }
}