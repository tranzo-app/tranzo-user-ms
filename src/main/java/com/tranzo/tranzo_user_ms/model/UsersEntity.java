
package com.tranzo.tranzo_user_ms.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tranzo.tranzo_user_ms.enums.AccountStatus;
import com.tranzo.tranzo_user_ms.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users" , uniqueConstraints = {
    @UniqueConstraint(name = "uk_app_user_mobile",columnNames = {"country_code", "mobile_number"}),
    @UniqueConstraint(name = "uk_app_user_email",columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UsersEntity {
    @Id
    @Column(name = "user_uuid" , updatable = false, nullable = false)
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userUuid;

    @Pattern(regexp = "\\+[0-9]{1,4}")
    private String countryCode;

    @Column(name = "email")
    private String email;

    @Column(name = "mobile_number" , nullable = false)
    private String mobileNumber;

    @Column(name = "created_at", updatable = false, nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @CreationTimestamp
    private LocalDateTime createdAt ;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @OneToOne(mappedBy = "user" , fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfileEntity userProfileEntity;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SocialHandleEntity> socialHandleEntity = new ArrayList<>();

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private VerificationEntity verificationEntity;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RefreshTokenEntity> refreshTokenEntities = new ArrayList<>();
}