package com.adidos.voucher;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    @GetMapping
    public String listVouchers() {

        return "admin/voucher/voucher_management";
    }
}