package com.tranzo.tranzo_user_ms.user.utility;

import com.tranzo.tranzo_user_ms.user.dto.VerifyOtpDto;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserUtility Unit Tests")
class UserUtilityTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserUtility userUtility;

    @Test
    @DisplayName("findUserByIdentifier with email in DTO returns user from repository")
    void findUserByIdentifier_dtoWithEmail_callsFindByEmail() {
        VerifyOtpDto dto = new VerifyOtpDto();
        dto.setEmailId("user@example.com");
        UsersEntity entity = new UsersEntity();
        entity.setUserUuid(UUID.randomUUID());
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(entity));

        Optional<UsersEntity> result = userUtility.findUserByIdentifier(dto);

        assertTrue(result.isPresent());
        assertEquals(entity, result.get());
        verify(userRepository).findByEmail("user@example.com");
        verify(userRepository, never()).findByMobileNumber(anyString());
    }

    @Test
    @DisplayName("findUserByIdentifier with mobile in DTO returns user from repository")
    void findUserByIdentifier_dtoWithMobile_callsFindByMobileNumber() {
        VerifyOtpDto dto = new VerifyOtpDto();
        dto.setMobileNumber("1234567890");
        UsersEntity entity = new UsersEntity();
        when(userRepository.findByMobileNumber("1234567890")).thenReturn(Optional.of(entity));

        Optional<UsersEntity> result = userUtility.findUserByIdentifier(dto);

        assertTrue(result.isPresent());
        verify(userRepository).findByMobileNumber("1234567890");
    }

    @Test
    @DisplayName("findUserByIdentifier with String email returns user from repository")
    void findUserByIdentifier_stringEmail_callsFindByEmail() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());

        Optional<UsersEntity> result = userUtility.findUserByIdentifier("test@test.com");

        assertTrue(result.isEmpty());
        verify(userRepository).findByEmail("test@test.com");
    }

    @Test
    @DisplayName("findUserByIdentifier with null or blank returns empty")
    void findUserByIdentifier_nullOrBlank_returnsEmpty() {
        assertTrue(userUtility.findUserByIdentifier(null).isEmpty());
        assertTrue(userUtility.findUserByIdentifier("").isEmpty());
        assertTrue(userUtility.findUserByIdentifier("   ").isEmpty());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).findByMobileNumber(anyString());
    }

    @Test
    @DisplayName("findUserByIdentifier with mobile string calls findByMobileNumber")
    void findUserByIdentifier_stringMobile_callsFindByMobileNumber() {
        when(userRepository.findByMobileNumber(anyString())).thenReturn(Optional.empty());

        userUtility.findUserByIdentifier("+911234567890");

        verify(userRepository).findByMobileNumber(anyString());
    }
}
