package com.adidos.user.controller;

import com.adidos.user.dto.AddressRequest;
import com.adidos.user.dto.ProfileUpdateRequest;
import com.adidos.user.service.AddressService;
import com.adidos.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

}