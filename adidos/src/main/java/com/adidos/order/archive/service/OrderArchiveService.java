package com.adidos.order.archive.service;

import com.adidos.order.archive.dto.*;
import com.adidos.order.archive.entity.OrderArchiveLog;
import com.adidos.order.archive.repository.OrderArchiveLogRepository;
import com.adidos.order.entity.Order;
import com.adidos.order.entity.OrderItem;
import com.adidos.order.entity.Payment;
import com.adidos.order.enums.OrderStatus;
import com.adidos.order.enums.PaymentStatus;
import com.adidos.order.repository.OrderItemRepository;
import com.adidos.order.repository.OrderRepository;
import com.adidos.order.repository.PaymentRepository;
import com.adidos.review.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;import org.springframework.web.multipart.MultipartFile;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class OrderArchiveService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final OrderArchiveLogRepository archiveLogRepository;

    @Value("${order.archive.path}")
    private String archivePath;




    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private OrderArchiveFileDto readArchiveFile(MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename();

        if (filename == null) {
            throw new RuntimeException("Tên file không hợp lệ");
        }

        if (filename.endsWith(".json")) {
            return objectMapper.readValue(file.getInputStream(), OrderArchiveFileDto.class);
        }

        if (filename.endsWith(".zip")) {
            try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry;

                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().endsWith(".json")) {
                        return objectMapper.readValue(zis, OrderArchiveFileDto.class);
                    }
                }
            }

            throw new RuntimeException("File ZIP không chứa JSON archive");
        }

        throw new RuntimeException("Chỉ hỗ trợ file .zip hoặc .json");
    }

    @Transactional
    public int importArchivedOrders(MultipartFile file) {
        try {
            OrderArchiveFileDto archive = readArchiveFile(file);

            if (archive.getOrders() == null || archive.getOrders().isEmpty()) {
                throw new RuntimeException("File archive không có đơn hàng");
            }

            int imported = 0;

            for (ArchivedOrderDto archived : archive.getOrders()) {

                // Không import đơn thiếu orderCode vì không thể chống trùng
                if (archived.getOrderCode() == null) {
                    continue;
                }

                // Nếu orderCode đã tồn tại thì bỏ qua
                if (orderRepository.existsByOrderCode(archived.getOrderCode())) {
                    continue;
                }

                Order order = Order.builder()
                        .orderCode(archived.getOrderCode())
                        .receiverName(archived.getReceiverName())
                        .receiverPhone(archived.getReceiverPhone())
                        .shippingAddress(archived.getShippingAddress())
                        .guestName(archived.getGuestName())
                        .guestEmail(archived.getGuestEmail())
                        .guestPhone(archived.getGuestPhone())
                        .guestAddress(archived.getGuestAddress())
                        .totalPrice(archived.getTotalPrice())
                        .shippingFee(archived.getShippingFee())
                        .discountAmount(archived.getDiscountAmount())
                        .orderStatus(OrderStatus.DELIVERED)
                        .createdAt(archived.getCreatedAt())
                        .build();

                order.setRestoredFromArchive(true);
                order.setRestoredAt(LocalDateTime.now());

                Order savedOrder = orderRepository.save(order);

                if (archived.getPayment() != null) {
                    ArchivedPaymentDto p = archived.getPayment();

                    Payment payment = Payment.builder()
                            .order(savedOrder)
                            .paymentMethod(p.getPaymentMethod())
                            .transactionCode(p.getTransactionCode())
                            .amount(p.getAmount())
                            .status(p.getStatus() != null
                                    ? PaymentStatus.valueOf(p.getStatus())
                                    : PaymentStatus.PAID)
                            .checkoutUrl(p.getCheckoutUrl())
                            .build();

                    paymentRepository.save(payment);
                    savedOrder.setPayment(payment);
                }

                if (archived.getItems() != null) {
                    for (ArchivedOrderItemDto itemDto : archived.getItems()) {
                        OrderItem item = OrderItem.builder()
                                .order(savedOrder)
                                .productName(itemDto.getProductName())
                                .price(itemDto.getPrice())
                                .quantity(itemDto.getQuantity())
                                .color(itemDto.getColor())
                                .size(itemDto.getSize())
                                .build();

                        orderItemRepository.save(item);
                    }
                }

                imported++;
            }

            return imported;

        } catch (Exception e) {
            throw new RuntimeException("Import archive thất bại: " + e.getMessage(), e);
        }
    }

    @Transactional
    public OrderArchiveLog archiveDeliveredOrdersBefore(LocalDateTime archivedUntil, String adminEmail) {
        List<Order> orders = orderRepository.findDeliveredOrdersForArchive(archivedUntil);

        if (orders.isEmpty()) {
            return archiveLogRepository.save(OrderArchiveLog.builder()
                    .archivedUntil(archivedUntil)
                    .totalOrders(0)
                    .createdBy(adminEmail)
                    .status("EMPTY")
                    .message("Không có đơn DELIVERED nào cần lưu trữ")
                    .build());
        }

        List<Long> orderIds = orders.stream().map(Order::getId).toList();

        try {
            OrderArchiveFileDto archiveDto = OrderArchiveFileDto.builder()
                    .archiveVersion("1.0")
                    .archivedAt(LocalDateTime.now())
                    .archivedUntil(archivedUntil)
                    .totalOrders(orders.size())
                    .orders(orders.stream().map(this::toArchivedOrder).toList())
                    .build();

            Path dir = Paths.get(archivePath);
            Files.createDirectories(dir);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            String jsonFileName = "orders_archive_" + timestamp + ".json";
            String zipFileName = "orders_archive_" + timestamp + ".zip";

            Path jsonPath = dir.resolve(jsonFileName);
            Path zipPath = dir.resolve(zipFileName);

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonPath.toFile(), archiveDto);

            zipFile(jsonPath, zipPath, jsonFileName);

            Files.deleteIfExists(jsonPath);

            String checksum = sha256(zipPath);

            OrderArchiveLog log = archiveLogRepository.save(OrderArchiveLog.builder()
                    .fileName(zipFileName)
                    .filePath(zipPath.toString())
                    .archivedUntil(archivedUntil)
                    .totalOrders(orders.size())
                    .checksum(checksum)
                    .createdBy(adminEmail)
                    .status("SUCCESS")
                    .message("Đã export ZIP thành công")
                    .build());

            reviewRepository.detachOrderItemsByOrderIds(orderIds);
            paymentRepository.deleteByOrderIds(orderIds);
            orderItemRepository.deleteByOrderIds(orderIds);
            orderRepository.deleteByIdInBatchCustom(orderIds);

            return log;

        } catch (Exception e) {
            archiveLogRepository.save(OrderArchiveLog.builder()
                    .archivedUntil(archivedUntil)
                    .totalOrders(orders.size())
                    .createdBy(adminEmail)
                    .status("FAILED")
                    .message(e.getMessage())
                    .build());

            throw new RuntimeException("Archive thất bại: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<OrderArchiveLog> getLogs() {
        return archiveLogRepository.findAllByOrderByCreatedAtDesc();
    }

    private ArchivedOrderDto toArchivedOrder(Order order) {
        Payment payment = order.getPayment();

        return ArchivedOrderDto.builder()
                .oldOrderId(order.getId())
                .orderCode(order.getOrderCode())

                .oldUserId(order.getUser() != null ? order.getUser().getId() : null)
                .userEmail(order.getUser() != null ? order.getUser().getEmail() : null)
                .userFullName(order.getUser() != null ? order.getUser().getFullName() : null)
                .userPhone(order.getUser() != null ? order.getUser().getPhone() : null)

                .oldVoucherId(order.getVoucher() != null ? order.getVoucher().getId() : null)
                .voucherCode(order.getVoucher() != null ? order.getVoucher().getCode() : null)

                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .shippingAddress(order.getShippingAddress())

                .guestName(order.getGuestName())
                .guestEmail(order.getGuestEmail())
                .guestPhone(order.getGuestPhone())
                .guestAddress(order.getGuestAddress())

                .totalPrice(order.getTotalPrice())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())

                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus().name() : null)
                .createdAt(order.getCreatedAt())

                .payment(payment != null ? toArchivedPayment(payment) : null)
                .items(order.getOrderItems() == null
                        ? List.of()
                        : order.getOrderItems().stream().map(this::toArchivedItem).toList())
                .build();
    }

    private ArchivedOrderItemDto toArchivedItem(OrderItem item) {
        return ArchivedOrderItemDto.builder()
                .oldOrderItemId(item.getId())
                .oldProductVariantId(item.getProductVariant() != null ? item.getProductVariant().getId() : null)
                .oldProductId(
                        item.getProductVariant() != null && item.getProductVariant().getProduct() != null
                                ? item.getProductVariant().getProduct().getId()
                                : null
                )
                .productName(item.getProductName())
                .price(item.getPrice())
                .quantity(item.getQuantity())
                .color(item.getColor())
                .size(item.getSize())
                .build();
    }

    private ArchivedPaymentDto toArchivedPayment(Payment payment) {
        return ArchivedPaymentDto.builder()
                .oldPaymentId(payment.getId())
                .paymentMethod(payment.getPaymentMethod())
                .transactionCode(payment.getTransactionCode())
                .amount(payment.getAmount())
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .checkoutUrl(payment.getCheckoutUrl())
                .build();
    }

    private void zipFile(Path sourceFile, Path zipFile, String entryName) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile));
             InputStream is = Files.newInputStream(sourceFile)) {

            zos.putNextEntry(new ZipEntry(entryName));
            is.transferTo(zos);
            zos.closeEntry();
        }
    }

    private String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream is = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int read;

            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        return HexFormat.of().formatHex(digest.digest());
    }
}