package com.tranzo.tranzo_user_ms.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Table(name = "APP_USER" , uniqueConstraints = {
    @UniqueConstraint(name = "uk_app_user_mobile",columnNames = "mobileNumber"),
    @UniqueConstraint(name = "uk_app_user_email",columnNames = "email")
})
public class UsersEntity {
    @Id @Column(name = "uuid" , updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email")
    private String email;

    @Column(name = "mobileNumber" , nullable = false)
    private String mobileNumber;

    @Column(name = "created_at", updatable = false, nullable = false)
    @CreationTimestamp
    private Instant createdAt ;

    @Column(name = "modified_at" )
    @CreationTimestamp
    private Instant modifiedAt ;

    @OneToOne(mappedBy = "user" , fetch = FetchType.LAZY)
    private UserProfile userProfile;
}
