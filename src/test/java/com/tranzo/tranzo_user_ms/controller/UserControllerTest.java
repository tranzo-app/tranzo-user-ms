package com.tranzo.tranzo_user_ms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranzo.tranzo_user_ms.dto.UrlDto;
import com.tranzo.tranzo_user_ms.dto.UserProfileDto;
import com.tranzo.tranzo_user_ms.enums.Gender;
import com.tranzo.tranzo_user_ms.exception.InvalidUserIdException;
import com.tranzo.tranzo_user_ms.exception.UserAlreadyDeletedExeption;
import com.tranzo.tranzo_user_ms.exception.UserProfileNotFoundException;
import com.tranzo.tranzo_user_ms.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
// @Import(GlobalExceptionHandler.class) // uncomment if needed
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private UserProfileDto validProfileDto() {
        return UserProfileDto.builder()
                .firstName("Komal")
                .middleName("F")
                .lastName("Sharma")
                .mobileNumber("9876543210")
                .emailId("komal@example.com")
                .bio("Travel lover and product builder")
                .gender(Gender.FEMALE)
                .dob(LocalDate.of(1997, 1, 1))
                .location("Mumbai, India")
                .build();
    }

    private UrlDto validUrlDto() {
        return UrlDto.builder()
                .Url("https://cdn.tranzo.com/profile/12345.png")
                .build();
    }

    // =====================================================================
    // GET /user/{userId}
    // =====================================================================

    @Nested
    @DisplayName("GET /user/{userId}")
    class GetUserTests {

        @Test
        @DisplayName("should return 200 with profile data on success")
        void getUser_success() throws Exception {
            String userId = "5b48b1e7-7e33-4a32-b3c8-7fd37e13c111";
            UserProfileDto dto = validProfileDto();

            when(userService.getUserProfile(eq(userId))).thenReturn(dto);

            mockMvc.perform(get("/user/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.statusMessage").value("User profile retrieved successfully"))
                    .andExpect(jsonPath("$.data.firstName").value("Komal"));
        }

        @Test
        @DisplayName("should return 400 when userId is invalid UUID")
        void getUser_invalidUserId() throws Exception {
            when(userService.getUserProfile(eq("bad-uuid")))
                    .thenThrow(new InvalidUserIdException("Invalid user UUID: bad-uuid"));

            mockMvc.perform(get("/user/{userId}", "bad-uuid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.statusMessage").value("Invalid user UUID: bad-uuid"))
                    .andExpect(jsonPath("$.data").doesNotExist());
        }

        @Test
        @DisplayName("should return 404 when profile not found")
        void getUser_notFound() throws Exception {
            String userId = "5b48b1e7-7e33-4a32-b3c8-7fd37e13c222";

            when(userService.getUserProfile(eq(userId)))
                    .thenThrow(new UserProfileNotFoundException("User profile not found for id: " + userId));

            mockMvc.perform(get("/user/{userId}", userId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.statusMessage").value("User profile not found for id: " + userId));
        }

        @Test
        @DisplayName("should return 400 when account is already deleted")
        void getUser_userDeleted() throws Exception {
            String userId = "5b48b1e7-7e33-4a32-b3c8-7fd37e13c333";

            when(userService.getUserProfile(eq(userId)))
                    .thenThrow(new UserAlreadyDeletedExeption("User account is deleted for id: " + userId));

            mockMvc.perform(get("/user/{userId}", userId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.statusMessage").value("User account is deleted for id: " + userId));
        }


    // =====================================================================
    // PATCH /user/{userId}
    // =====================================================================



        @Test
        @DisplayName("should return 200 with updated profile on success")
        void patchUser_success() throws Exception {
            String userId = "5b48b1e7-7e33-4a32-b3c8-7fd37e13c444";
            UserProfileDto input = validProfileDto();
            UserProfileDto updated = validProfileDto();
            updated.setBio("Travel lover and product builder");

            when(userService.updateUserProfile(eq(userId), any(UserProfileDto.class))).thenReturn(updated);

            mockMvc.perform(patch("/user/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.statusMessage").value("User profile retrieved successfully"))
                    .andExpect(jsonPath("$.data.bio").value("Travel lover and product builder"));
        }

        @Test
        @DisplayName("should return 400 when body validation fails")
        void patchUser_validationError() throws Exception {
            // Missing required fields -> should fail @Valid
            UserProfileDto invalid = UserProfileDto.builder()
                    .firstName("")  // @NotBlank
                    .mobileNumber("123") // pattern fails
                    .build();

            mockMvc.perform(patch("/user/{userId}", "5b48b1e7-7e33-4a32-b3c8-7fd37e13c555")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(400));
            // If you return field-level errors in data, you can assert them too.
        }

        @Test
        @DisplayName("should return 400 when userId is invalid UUID")
        void patchUser_invalidUserId() throws Exception {
            when(userService.updateUserProfile(eq("bad-uuid"), any(UserProfileDto.class)))
                    .thenThrow(new InvalidUserIdException("Invalid user UUID: bad-uuid"));

            mockMvc.perform(patch("/user/{userId}", "bad-uuid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validProfileDto())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.statusMessage").value("Invalid user UUID: bad-uuid"));
        }

        @Test
        @DisplayName("should return 404 when profile not found")
        void patchUser_notFound() throws Exception {
            String userId = "5b48b1e7-7e33-4a32-b3c8-7fd37e13c666";

            when(userService.updateUserProfile(eq(userId), any(UserProfileDto.class)))
                    .thenThrow(new UserProfileNotFoundException("User profile not found for id: " + userId));

            mockMvc.perform(patch("/user/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validProfileDto())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.statusMessage").value("User profile not found for id: " + userId));
        }

        @Test
        @DisplayName("should return 400 when account already deleted")
        void patchUser_userDeleted() throws Exception {
            String userId = "5b48b1e7-7e33-4a32-b3c8-7fd37e13c777";

            when(userService.updateUserProfile(eq(userId), any(UserProfileDto.class)))
                    .thenThrow(new UserAlreadyDeletedExeption("User account is deleted for id: " + userId));

            mockMvc.perform(patch("/user/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validProfileDto())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.statusMessage").value("User account is deleted for id: " + userId));
        }


    // =====================================================================
    // DELETE /user/{userId}
    // =====================================================================


        @Test
        @DisplayName("should return 200 when delete succeeds")
        void deleteUser_success() throws Exception {
            String userId = "5b48b1e7-7e33-4a32-b3c8-7fd37e13c888";

            doNothing().when(userService).deleteUserProfile(eq(userId));

            mockMvc.perform(delete("/user/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.statusMessage").value("User profile retrieved successfully"))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("should return 400 when userId is invalid UUID")
        void deleteUser_invalidUserId() throws Exception {
            doThrow(new InvalidUserIdException("Invalid user UUID: bad-uuid"))
                    .when(userService).deleteUserProfile(eq("bad-uuid"));

            mockMvc.perform(delete("/user/{userId}", "bad-uuid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.statusMessage").value("Invalid user UUID: bad-uuid"));
        }

        @Test
        @DisplayName("should return 404 when profile not found")
        void deleteUser_notFound() throws Exception {
            String userId = "5b48b1e7-7e33-4a32-b3c8-7fd37e13c999";

            doThrow(new UserProfileNotFoundException("User profile not found for id: " + userId))
                    .when(userService).deleteUserProfile(eq(userId));

            mockMvc.perform(delete("/user/{userId}", userId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.statusMessage").value("User profile not found for id: " + userId));
        }

        @Test
        @DisplayName("should return 400 when account already deleted")
        void deleteUser_userDeleted() throws Exception {
            String userId = "5b48b1e7-7e33-4a32-b3c8-7fd37e13caaa";

            doThrow(new UserAlreadyDeletedExeption("User account is deleted for id: " + userId))
                    .when(userService).deleteUserProfile(eq(userId));

            mockMvc.perform(delete("/user/{userId}", userId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.statusMessage").value("User account is deleted for id: " + userId));
        }
    }

    // =====================================================================
    // PUT /user/{userId}/profile-picture
    // =====================================================================

        @Test
        @DisplayName("should return 200 when profile picture updated successfully")
        void updatePicture_success() throws Exception {
            String userId = "5b48b1e7-7e33-4a32-b3c8-7fd37e13cbbb";
            UserProfileDto updated = validProfileDto();
            updated.setProfilePictureUrl("https://cdn.tranzo.com/profile/new.png");

            when(userService.updateProfilePicture(eq(userId), any(UrlDto.class))).thenReturn(updated);

            mockMvc.perform(put("/user/{userId}/profile-picture", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUrlDto())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.statusMessage").value("User profile retrieved successfully"))
                    .andExpect(jsonPath("$.data.profilePictureUrl").value("https://cdn.tranzo.com/profile/new.png"));
        }

        @Test
        @DisplayName("should return 400 when URL is invalid (validation)")
        void updatePicture_validationError() throws Exception {
            UrlDto invalid = UrlDto.builder()
                    .Url("not-a-url")
                    .build();

            mockMvc.perform(put("/user/{userId}/profile-picture", "5b48b1e7-7e33-4a32-b3c8-7fd37e13cccc")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(400));
        }

        @Test
        @DisplayName("should return 400 when userId is invalid UUID")
        void updatePicture_invalidUserId() throws Exception {
            when(userService.updateProfilePicture(eq("bad-uuid"), any(UrlDto.class)))
                    .thenThrow(new InvalidUserIdException("Invalid user UUID: bad-uuid"));

            mockMvc.perform(put("/user/{userId}/profile-picture", "bad-uuid")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUrlDto())))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.statusMessage").value("Invalid user UUID: bad-uuid"));
        }

        @Test
        @DisplayName("should return 404 when profile not found")
        void updatePicture_notFound() throws Exception {
            String userId = "5b48b1e7-7e33-4a32-b3c8-7fd37e13cddd";

            when(userService.updateProfilePicture(eq(userId), any(UrlDto.class)))
                    .thenThrow(new UserProfileNotFoundException("User profile not found for id: " + userId));

            mockMvc.perform(put("/user/{userId}/profile-picture", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUrlDto())))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.statusMessage").value("User profile not found for id: " + userId));
        }


    // =====================================================================
    // DELETE /user/{userId}/profile-picture
    // =====================================================================


        @Test
        @DisplayName("should return 200 when profile picture deleted successfully")
        void deletePicture_success() throws Exception {
            String userId = "5b48b1e7-7e33-4a32-b3c8-7fd37e13ceee";
            UserProfileDto updated = validProfileDto();
            updated.setProfilePictureUrl(null);

            when(userService.deleteProfilePicture(eq(userId))).thenReturn(updated);

            mockMvc.perform(delete("/user/{userId}/profile-picture", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.statusMessage").value("User profile retrieved successfully"))
                    .andExpect(jsonPath("$.data.profilePictureUrl").doesNotExist());
        }

        @Test
        @DisplayName("should return 400 when userId is invalid UUID")
        void deletePicture_invalidUserId() throws Exception {
            doThrow(new InvalidUserIdException("Invalid user UUID: bad-uuid"))
                    .when(userService).deleteProfilePicture(eq("bad-uuid"));

            mockMvc.perform(delete("/user/{userId}/profile-picture", "bad-uuid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(400))
                    .andExpect(jsonPath("$.statusMessage").value("Invalid user UUID: bad-uuid"));
        }

        @Test
        @DisplayName("should return 404 when profile not found")
        void deletePicture_notFound() throws Exception {
            String userId = "5b48b1e7-7e33-4a32-b3c8-7fd37e13cfff";

            doThrow(new UserProfileNotFoundException("User profile not found for id: " + userId))
                    .when(userService).deleteProfilePicture(eq(userId));

            mockMvc.perform(delete("/user/{userId}/profile-picture", userId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.statusCode").value(404))
                    .andExpect(jsonPath("$.statusMessage").value("User profile not found for id: " + userId));
        }
}
