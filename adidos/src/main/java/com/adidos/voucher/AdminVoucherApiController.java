package com.adidos.voucher;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherApiController {

    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<List<Voucher>> getAll() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Voucher> getById(@PathVariable Long id) {
        return ResponseEntity.ok(voucherService.getVoucherById(id));
    }

    @PostMapping
    public ResponseEntity<Voucher> create(@RequestBody Voucher voucher) {
        return ResponseEntity.ok(voucherService.saveVoucher(voucher));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voucher> update(@PathVariable Long id, @RequestBody Voucher voucher) {
        voucher.setId(id);
        return ResponseEntity.ok(voucherService.saveVoucher(voucher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.ok().build();
    }
}