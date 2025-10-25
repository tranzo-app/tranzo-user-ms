package com.tranzo.tranzo_user_ms.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Entity
@Table(name = "USER_PROFILE")
@Data
public class UserProfile{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @Column(name = "ID", updatable = false, nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "USER_ID", nullable = false , unique = true , foreignKey = @ForeignKey(name = "fk_user_profile_user"))
    private UsersEntity user;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "MIDDLE_NAME")
    private String middleName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "PROFILE_PICTURE_URL")
    private String profilePictureUrl;

    @Column(name = "BIO")
    private String bio;

    @Column(name = "GENDER")
    private String gender;

    @Column(name = "AGE")
    private int age;

    @Column(name = "CITY")
    private String city;

    @Column(name = "FACEBOOK_URL")
    private String facebookUrl;

    @Column(name = "INSTAGRAM_URL")
    private String instagramUrl;

}
