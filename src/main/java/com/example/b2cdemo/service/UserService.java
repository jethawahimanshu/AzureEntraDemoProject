package com.example.b2cdemo.service;

import com.example.b2cdemo.dto.request.UserProfileRequest;
import com.example.b2cdemo.dto.response.UserProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserService {

    UserProfileResponse getProfile(String oid, Jwt jwt);

    UserProfileResponse upsertProfile(String oid, Jwt jwt, UserProfileRequest request);

    void deleteProfile(String oid);

    Page<UserProfileResponse> findAllUsers(Pageable pageable);

    UserProfileResponse getProfileByOid(String oid);
}
