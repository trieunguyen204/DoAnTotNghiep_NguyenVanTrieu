package com.adidos.tryon.controller;

import com.adidos.tryon.dto.TryOnResponse;
import com.adidos.tryon.service.VirtualTryOnService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/try-on")
@RequiredArgsConstructor
public class VirtualTryOnController {

    private final VirtualTryOnService virtualTryOnService;

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(
            @RequestParam Long productId,
            HttpSession session
    ) {
        return ResponseEntity.ok(
                virtualTryOnService.getHistory(productId, session.getId())
        );
    }

    @PostMapping
    public ResponseEntity<?> createTryOn(
            @RequestParam Long productId,
            @RequestParam Long variantId,
            @RequestParam("personImage") MultipartFile personImage,
            HttpSession session
    ) {
        TryOnResponse response = virtualTryOnService.createTryOn(
                productId,
                variantId,
                personImage,
                session.getId()
        );
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getTryOn(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(virtualTryOnService.getTryOn(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
