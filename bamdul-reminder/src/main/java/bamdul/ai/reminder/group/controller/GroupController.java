package bamdul.ai.reminder.group.controller;

import bamdul.ai.reminder.group.service.dto.CreateGroupCommand;
import bamdul.ai.reminder.group.service.dto.GroupMemberResult;
import bamdul.ai.reminder.group.service.dto.GroupResult;
import bamdul.ai.reminder.group.service.dto.InviteMemberCommand;
import bamdul.ai.reminder.group.service.dto.UpdateGroupCommand;
import bamdul.ai.reminder.group.service.dto.UpdatePermissionCommand;
import bamdul.ai.reminder.group.service.port.in.GroupService;
import bamdul.ai.reminder.reminderlist.service.dto.ReminderListResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResult create(@RequestBody CreateGroupCommand command, @AuthenticationPrincipal Long memberId) {
        return groupService.create(command, memberId);
    }

    @GetMapping
    public List<GroupResult> findAll(@AuthenticationPrincipal Long memberId) {
        return groupService.findAll(memberId);
    }

    @GetMapping("/{id}")
    public GroupResult findById(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
        return groupService.findById(id, memberId);
    }

    @PutMapping("/{id}")
    public GroupResult update(@PathVariable Long id, @RequestBody UpdateGroupCommand command, @AuthenticationPrincipal Long memberId) {
        return groupService.update(id, command, memberId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
        groupService.delete(id, memberId);
    }

    // Group members
    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupMemberResult inviteMember(@PathVariable Long id, @RequestBody InviteMemberCommand command, @AuthenticationPrincipal Long memberId) {
        return groupService.inviteMember(id, command, memberId);
    }

    @DeleteMapping("/{id}/members/{targetMemberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable Long id, @PathVariable Long targetMemberId, @AuthenticationPrincipal Long memberId) {
        groupService.removeMember(id, targetMemberId, memberId);
    }

    @PatchMapping("/{id}/members/{targetMemberId}/permission")
    public GroupMemberResult updatePermission(@PathVariable Long id, @PathVariable Long targetMemberId,
                                               @RequestBody UpdatePermissionCommand command, @AuthenticationPrincipal Long memberId) {
        return groupService.updatePermission(id, targetMemberId, command, memberId);
    }

    @GetMapping("/{id}/members")
    public List<GroupMemberResult> findMembers(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
        return groupService.findMembers(id, memberId);
    }

    // Group lists
    @GetMapping("/{id}/lists")
    public List<ReminderListResult> findGroupLists(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
        return groupService.findGroupLists(id, memberId);
    }
}
