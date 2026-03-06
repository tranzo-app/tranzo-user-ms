package com.tranzo.tranzo_user_ms.splitwise.dto.request;

import com.tranzo.tranzo_user_ms.splitwise.entity.GroupMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

/**
 * DTO for adding members to a group.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddGroupMemberRequest {

    @NotEmpty(message = "At least one member ID is required")
    private List<UUID> memberIds;

    private GroupMember.MemberRole role = GroupMember.MemberRole.MEMBER;
}
