package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.request.AddGroupMemberRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateGroupRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.GroupResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.GroupMember;
import com.tranzo.tranzo_user_ms.splitwise.entity.SplitwiseGroup;
import com.tranzo.tranzo_user_ms.splitwise.exception.GroupNotFoundException;
import com.tranzo.tranzo_user_ms.splitwise.exception.UserNotMemberException;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SplitwiseGroupService Unit Tests")
class SplitwiseGroupServiceTest {

    @Mock
    private SplitwiseGroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityService activityService;

    @InjectMocks
    private SplitwiseGroupService groupService;

    private UUID creatorId;
    private UUID memberId;
    private UsersEntity creator;
    private SplitwiseGroup group;
    private CreateGroupRequest createRequest;
    private AddGroupMemberRequest addMemberRequest;

    @BeforeEach
    void setUp() {
        creatorId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        creator = new UsersEntity();
        creator.setUserUuid(creatorId);
        creator.setEmail("creator@test.com");
        creator.setCountryCode("+91");
        creator.setMobileNumber("9999999999");
        UserProfileEntity profile = new UserProfileEntity();
        profile.setFirstName("Creator");
        profile.setLastName("User");
        creator.setUserProfileEntity(profile);

        group = SplitwiseGroup.builder()
                .id(1L)
                .tripId(UUID.randomUUID())
                .description("Test group")
                .createdBy(creatorId)
                .build();
        GroupMember adminMember = GroupMember.builder()
                .group(group)
                .userId(creatorId)
                .role(GroupMember.MemberRole.ADMIN)
                .build();
        group.addMember(adminMember);

        createRequest = CreateGroupRequest.builder()
                .name("Test Group")
                .description("Desc")
                .memberIds(List.of(memberId))
                .build();

        addMemberRequest = AddGroupMemberRequest.builder()
                .memberIds(List.of(memberId))
                .build();
    }

    @Test
    @DisplayName("Should create group successfully")
    void createGroup_Success() {
        UsersEntity member = new UsersEntity();
        member.setUserUuid(memberId);
        member.setEmail("member@test.com");
        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(userRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(groupRepository.save(any(SplitwiseGroup.class))).thenReturn(group);

        GroupResponse response = groupService.createGroup(createRequest, creatorId);

        assertNotNull(response);
        assertEquals(group.getId(), response.getId());
        verify(groupRepository).save(any(SplitwiseGroup.class));
        verify(activityService).logGroupCreated(eq(creator), any(SplitwiseGroup.class));
    }

    @Test
    @DisplayName("Should throw when creator not found")
    void createGroup_CreatorNotFound() {
        when(userRepository.findById(creatorId)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> groupService.createGroup(createRequest, creatorId));
        verify(groupRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get group when user is member")
    void getGroup_Success() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        GroupResponse response = groupService.getGroup(1L, creatorId);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    @DisplayName("Should throw GroupNotFoundException when group not found")
    void getGroup_NotFound() {
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(GroupNotFoundException.class, () -> groupService.getGroup(999L, creatorId));
    }

    @Test
    @DisplayName("Should throw UserNotMemberException when user not member")
    void getGroup_UserNotMember() {
        UUID nonMemberId = UUID.randomUUID();
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        assertThrows(UserNotMemberException.class, () -> groupService.getGroup(1L, nonMemberId));
    }

    @Test
    @DisplayName("Should add members when current user is admin")
    void addMembers_Success() {
        UsersEntity newMember = new UsersEntity();
        newMember.setUserUuid(memberId);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupRepository.isUserAdminOfGroup(1L, creatorId)).thenReturn(true);
        when(userRepository.findById(memberId)).thenReturn(Optional.of(newMember));
        when(groupRepository.save(any(SplitwiseGroup.class))).thenReturn(group);

        GroupResponse response = groupService.addMembers(1L, addMemberRequest, creatorId);

        assertNotNull(response);
        verify(activityService).logMemberAdded(eq(memberId), eq(group), eq(creatorId));
    }

    @Test
    @DisplayName("Should remove member successfully")
    void removeMember_Success() {
        GroupMember toRemove = GroupMember.builder().group(group).userId(memberId).role(GroupMember.MemberRole.MEMBER).build();
        group.addMember(toRemove);
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupRepository.isUserAdminOfGroup(1L, creatorId)).thenReturn(true);
        when(userRepository.findById(memberId)).thenReturn(Optional.of(new UsersEntity()));
        when(groupRepository.save(any(SplitwiseGroup.class))).thenReturn(group);

        GroupResponse response = groupService.removeMember(1L, memberId, creatorId);

        assertNotNull(response);
        verify(activityService).logMemberRemoved(eq(memberId), eq(group), eq(creatorId));
    }

    @Test
    @DisplayName("Should get user groups")
    void getUserGroups_Success() {
        when(groupRepository.findByUserId(creatorId)).thenReturn(List.of(group));

        List<GroupResponse> list = groupService.getUserGroups(creatorId);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());
    }

    @Test
    @DisplayName("Should update group when user is admin")
    void updateGroup_Success() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupRepository.isUserAdminOfGroup(1L, creatorId)).thenReturn(true);
        when(groupRepository.save(any(SplitwiseGroup.class))).thenReturn(group);

        GroupResponse response = groupService.updateGroup(1L, createRequest, creatorId);

        assertNotNull(response);
        verify(activityService).logGroupUpdated(eq(group), eq(creatorId));
    }

    @Test
    @DisplayName("Should delete group when user is admin")
    void deleteGroup_Success() {
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupRepository.isUserAdminOfGroup(1L, creatorId)).thenReturn(true);
        doNothing().when(groupRepository).delete(any(SplitwiseGroup.class));

        groupService.deleteGroup(1L, creatorId);

        verify(groupRepository).delete(group);
        verify(activityService).logGroupDeleted(eq(group), eq(creatorId));
    }
}
