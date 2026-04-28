package com.adidos.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;


}