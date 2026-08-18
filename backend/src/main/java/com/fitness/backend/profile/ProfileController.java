package com.fitness.backend.profile;

import com.fitness.backend.profile.dto.ProfileRequest;
import com.fitness.backend.profile.dto.ProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody ProfileRequest request, Authentication authentication) {
        return ResponseEntity.ok(profileService.updateProfile(authentication.getName(), request));
    }
}
