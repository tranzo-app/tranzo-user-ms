package com.tranzo.tranzo_user_ms.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "aadhar_otp",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_uuid", "document_type"})
        }
)
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

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
