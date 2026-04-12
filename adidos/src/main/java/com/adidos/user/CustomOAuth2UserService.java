package com.adidos.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserProviderRepository providerRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String fullName = (String) attributes.get("name");
        String picture = (String) attributes.get("picture"); // Lấy thêm ảnh nếu có

        String providerId = attributes.containsKey("sub") ?
                (String) attributes.get("sub") :
                (String) attributes.get("id");

        User user = providerRepository.findByProviderAndProviderId(provider, providerId)
                .map(UserProvider::getUser)
                .orElseGet(() -> {
                    User existingUser = userRepository.findByEmail(email).orElseGet(() -> {
                        User newUser = User.builder()
                                .email(email)
                                .fullName(fullName)
                                .avatarUrl(picture)
                                .password(null)
                                .role("USER")
                                .status("ACTIVE")
                                .build();
                        return userRepository.save(newUser);
                    });

                    UserProvider newProvider = UserProvider.builder()
                            .user(existingUser)
                            .provider(provider)
                            .providerId(providerId)
                            .email(email)
                            .build();
                    providerRepository.save(newProvider);
                    return existingUser;
                });

        return oAuth2User;
    }
}