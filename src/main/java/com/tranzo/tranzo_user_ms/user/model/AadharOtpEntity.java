package com.tranzo.tranzo_user_ms.user.model;

import com.tranzo.tranzo_user_ms.user.enums.OtpStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "aadhar_otp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AadharOtpEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "reference_id", nullable = false)
    private String referenceId;

    @Column(name = "aadhaar_number", nullable = false)
    private String aadhaarNumber; // masked or encrypted

    private boolean used;

    @Enumerated(EnumType.STRING)
    @Column(name = "otp_status", nullable = false)
    private OtpStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
