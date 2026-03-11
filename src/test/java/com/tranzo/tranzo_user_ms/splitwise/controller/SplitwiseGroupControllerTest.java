package com.tranzo.tranzo_user_ms.splitwise.controller;

import com.tranzo.tranzo_user_ms.commons.utility.SecurityUtils;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.AddGroupMemberRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.request.CreateGroupRequest;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.GroupResponse;
import com.tranzo.tranzo_user_ms.splitwise.service.SplitwiseGroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SplitwiseGroupController Unit Tests")
class SplitwiseGroupControllerTest {

    @Mock
    private SplitwiseGroupService groupService;

    @InjectMocks
    private SplitwiseGroupController controller;

    private UUID userId;
    private Long groupId;
    private GroupResponse groupResponse;
    private CreateGroupRequest createRequest;
    private AddGroupMemberRequest addMemberRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        groupId = 1L;
        groupResponse = GroupResponse.builder().id(groupId).name("Test").description("Desc").members(List.of()).build();
        createRequest = CreateGroupRequest.builder().name("Test").description("Desc").memberIds(List.of(userId)).build();
        addMemberRequest = AddGroupMemberRequest.builder().memberIds(List.of(userId)).build();
    }

    @Test
    @DisplayName("Should create group and return 200")
    void createGroup_Success() throws Exception {
        when(groupService.createGroup(any(CreateGroupRequest.class), eq(userId))).thenReturn(groupResponse);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<GroupResponse> res = controller.createGroup(createRequest);

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertNotNull(res.getBody());
            assertEquals(groupId, res.getBody().getId());
        }
    }

    @Test
    @DisplayName("Should get group by id")
    void getGroup_Success() throws Exception {
        when(groupService.getGroup(groupId, userId)).thenReturn(groupResponse);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<GroupResponse> res = controller.getGroup(groupId);

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertNotNull(res.getBody());
        }
    }

    @Test
    @DisplayName("Should update group")
    void updateGroup_Success() throws Exception {
        when(groupService.updateGroup(eq(groupId), any(CreateGroupRequest.class), eq(userId))).thenReturn(groupResponse);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<GroupResponse> res = controller.updateGroup(groupId, createRequest);

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should delete group and return 204")
    void deleteGroup_Success() throws Exception {
        doNothing().when(groupService).deleteGroup(groupId, userId);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<Void> res = controller.deleteGroup(groupId);

            assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should get user groups")
    void getUserGroups_Success() throws Exception {
        when(groupService.getUserGroups(userId)).thenReturn(List.of(groupResponse));
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<List<GroupResponse>> res = controller.getUserGroups();

            assertEquals(HttpStatus.OK, res.getStatusCode());
            assertNotNull(res.getBody());
            assertEquals(1, res.getBody().size());
        }
    }

    @Test
    @DisplayName("Should add members")
    void addMembers_Success() throws Exception {
        when(groupService.addMembers(eq(groupId), any(AddGroupMemberRequest.class), eq(userId))).thenReturn(groupResponse);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<GroupResponse> res = controller.addMembers(groupId, addMemberRequest);

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should remove member")
    void removeMember_Success() throws Exception {
        UUID memberId = UUID.randomUUID();
        when(groupService.removeMember(groupId, memberId, userId)).thenReturn(groupResponse);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<GroupResponse> res = controller.removeMember(groupId, memberId);

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }

    @Test
    @DisplayName("Should get group members")
    void getGroupMembers_Success() throws Exception {
        when(groupService.getGroup(groupId, userId)).thenReturn(groupResponse);
        try (MockedStatic<SecurityUtils> security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getCurrentUserUuid).thenReturn(userId);

            ResponseEntity<GroupResponse> res = controller.getGroupMembers(groupId);

            assertEquals(HttpStatus.OK, res.getStatusCode());
        }
    }
}
