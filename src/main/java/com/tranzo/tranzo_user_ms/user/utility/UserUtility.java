package com.tranzo.tranzo_user_ms.user.utility;

import com.tranzo.tranzo_user_ms.user.dto.VerifyOtpDto;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserUtility {
    private final UserRepository userRepository;

    public Optional<UsersEntity> findUserByIdentifier(VerifyOtpDto dto) {
        if (dto.getEmailId() != null && !dto.getEmailId().isBlank()) {
            return userRepository.findByEmail(dto.getEmailId().toLowerCase());
        }
        return userRepository.findByMobileNumber(dto.getMobileNumber());
    }

    public Optional<UsersEntity> findUserByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        identifier = identifier.trim();
        if (isEmail(identifier)) {
            return userRepository.findByEmail(identifier.toLowerCase());
        }
        return userRepository.findByMobileNumber(normalizeMobile(identifier));
    }


    private boolean isEmail(String value) {
        return value.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private String normalizeMobile(String mobile) {
        return mobile.replaceAll("\\s+", "");
    }
}
