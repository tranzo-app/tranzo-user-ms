package com.tranzo.tranzo_user_ms.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tranzo.tranzo_user_ms.enums.Gender;
import com.tranzo.tranzo_user_ms.enums.VerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "user_profile_uuid", updatable = false, nullable = false)
    private UUID userProfileUuid;

    @OneToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "user_uuid", nullable = false , unique = true , foreignKey = @ForeignKey(name = "fk_user_profile_user"))
    private UsersEntity user;

    @Column(name = "FIRST_NAME", nullable = false)
    private String firstName;

    @Column(name = "MIDDLE_NAME")
    private String middleName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "PROFILE_PICTURE_URL")
    private String profilePictureUrl;

    @Column(name = "BIO")
    private String bio;

    @Column(name = "GENDER", nullable = false)
    private Gender gender;

    @Past
    @Column(name = "DATE_OF_BIRTH", nullable = false)
    private LocalDate dob;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "created_at", updatable = false, nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @CreationTimestamp
    private LocalDateTime createdAt ;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus;

}
