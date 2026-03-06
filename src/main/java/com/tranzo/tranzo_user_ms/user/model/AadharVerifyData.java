package com.tranzo.tranzo_user_ms.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AadharVerifyData {
    @JsonProperty("@entity")
    private String entity;

    @JsonProperty("reference_id")
    private Long referenceId;

    private String status; // VALID

    private String message;

    private String name;

    @JsonProperty("date_of_birth")
    private String dateOfBirth;

    private String gender;

    @JsonProperty("photo")
    private String photo;

    @JsonProperty("share_code")
    private String shareCode;
}