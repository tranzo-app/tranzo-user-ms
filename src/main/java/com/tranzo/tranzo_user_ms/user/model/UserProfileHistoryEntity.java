package com.tranzo.tranzo_user_ms.user.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tranzo.tranzo_user_ms.user.enums.Gender;
import com.tranzo.tranzo_user_ms.user.enums.VerificationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_profile_history_table",uniqueConstraints = @UniqueConstraint(
        name = "uk_user_profile_history_user_profile_uuid_version",
        columnNames = {"user_profile_uuid", "version"}
))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserProfileHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "user_profile_history_id", updatable = false, nullable = false)
    private UUID historyId;

    @Column(name = "user_profile_uuid", updatable = false, nullable = false)
    private UUID userProfileUuid;

    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "user_uuid", nullable = false , foreignKey = @ForeignKey(name = "fk_user_profile_user"))
    private UsersEntity user;

    @Column(name = "FIRST_NAME", nullable = false, updatable = false)
    private String firstName;

    @Column(name = "MIDDLE_NAME", updatable = false)
    private String middleName;

    @Column(name = "LAST_NAME", updatable = false)
    private String lastName;

    @Column(name = "PROFILE_PICTURE_URL", updatable = false)
    private String profilePictureUrl;

    @Column(name = "BIO", updatable = false)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENDER", nullable = false, updatable = false)
    private Gender gender;

    @Past
    @Column(name = "DATE_OF_BIRTH", nullable = false, updatable = false)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, updatable = false)
    private VerificationStatus verificationStatus;

    @Column(name = "version", nullable = false, updatable = false)
    private Integer profileVersion;
}
