package com.tranzo.tranzo_user_ms.user.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tranzo.tranzo_user_ms.user.enums.DocumentType;
import com.tranzo.tranzo_user_ms.user.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "verification",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_uuid", "document_type"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VerificationEntity {
    @Id
    @EqualsAndHashCode.Include
    @Column(name = "verification_uuid" , updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID verificationUuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", nullable = false)
    private UsersEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 20)
    private String documentNumber; // masked

    @Column(name = "provider_reference_id")
    private String providerReferenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private VerificationStatus verificationStatus;

    @Lob
    @Column(name = "verification_remarks")
    private String verificationRemarks;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private String verifiedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}