package com.example.b2cdemo.service;

import com.example.b2cdemo.domain.entity.User;
import com.example.b2cdemo.dto.request.UserProfileRequest;
import com.example.b2cdemo.dto.response.UserProfileResponse;
import com.example.b2cdemo.exception.ResourceNotFoundException;
import com.example.b2cdemo.repository.UserRepository;
import com.example.b2cdemo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String oid, Jwt jwt) {
        return userRepository.findById(oid)
                .map(this::toResponse)
                .orElseGet(() -> buildResponseFromJwt(oid, jwt));
    }

    @Override
    @Transactional
    public UserProfileResponse upsertProfile(String oid, Jwt jwt, UserProfileRequest request) {
        User user = userRepository.findById(oid)
                .orElseGet(() -> buildNewUser(oid, jwt));

        if (request.displayName() != null) {
            user.setDisplayName(request.displayName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }

        User saved = userRepository.save(user);
        log.debug("Upserted profile for oid={}", oid);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProfile(String oid) {
        if (!userRepository.existsById(oid)) {
            throw new ResourceNotFoundException("User", oid);
        }
        userRepository.deleteById(oid);
        log.info("Deleted profile for oid={}", oid);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileResponse> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByOid(String oid) {
        return userRepository.findById(oid)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User", oid));
    }

    private User buildNewUser(String oid, Jwt jwt) {
        return User.builder()
                .oid(oid)
                .displayName(SecurityUtils.extractDisplayName(jwt).orElse(null))
                .email(SecurityUtils.extractEmail(jwt).orElse(null))
                .build();
    }

    private UserProfileResponse buildResponseFromJwt(String oid, Jwt jwt) {
        return new UserProfileResponse(
                oid,
                SecurityUtils.extractDisplayName(jwt).orElse(null),
                SecurityUtils.extractEmail(jwt).orElse(null),
                null,
                null,
                null
        );
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getOid(),
                user.getDisplayName(),
                user.getEmail(),
                user.getPhone(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
