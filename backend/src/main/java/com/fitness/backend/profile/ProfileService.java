package com.fitness.backend.profile;

import com.fitness.backend.profile.dto.ProfileRequest;
import com.fitness.backend.profile.dto.ProfileResponse;
import com.fitness.backend.user.User;
import com.fitness.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        User user = getUserByEmail(email);
        return toResponse(user);
    }

    @Transactional
    public ProfileResponse updateProfile(String email, ProfileRequest request) {
        User user = getUserByEmail(email);

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName());
        }
        user.setAge(request.age());
        user.setWeightKg(request.weightKg());
        user.setHeightCm(request.heightCm());
        user.setSex(request.sex());
        user.setActivityLevel(request.activityLevel());

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private ProfileResponse toResponse(User user) {
        return new ProfileResponse(
            user.getEmail(),
            user.getFullName(),
            user.getAge(),
            user.getWeightKg(),
            user.getHeightCm(),
            user.getSex(),
            user.getActivityLevel()
        );
    }
}
