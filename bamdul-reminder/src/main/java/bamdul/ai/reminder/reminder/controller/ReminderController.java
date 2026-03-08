package bamdul.ai.reminder.reminder.controller;

import bamdul.ai.reminder.reminder.service.dto.CreateReminderCommand;
import bamdul.ai.reminder.reminder.service.dto.ReminderResult;
import bamdul.ai.reminder.reminder.service.dto.ReorderReminderCommand;
import bamdul.ai.reminder.reminder.service.dto.UpdateReminderCommand;
import bamdul.ai.reminder.reminder.service.port.in.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @GetMapping("/api/lists/{listId}/reminders")
    public List<ReminderResult> findAllByListId(@PathVariable Long listId, @AuthenticationPrincipal Long memberId) {
        return reminderService.findAllByListId(listId, memberId);
    }

    @PostMapping("/api/lists/{listId}/reminders")
    @ResponseStatus(HttpStatus.CREATED)
    public ReminderResult create(@PathVariable Long listId, @RequestBody CreateReminderCommand command,
                                 @AuthenticationPrincipal Long memberId) {
        return reminderService.create(listId, command, memberId);
    }

    @GetMapping("/api/reminders/{id}")
    public ReminderResult findById(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
        return reminderService.findById(id, memberId);
    }

    @PutMapping("/api/reminders/{id}")
    public ReminderResult update(@PathVariable Long id, @RequestBody UpdateReminderCommand command,
                                 @AuthenticationPrincipal Long memberId) {
        return reminderService.update(id, command, memberId);
    }

    @DeleteMapping("/api/reminders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
        reminderService.delete(id, memberId);
    }

    @PatchMapping("/api/reminders/{id}/toggle")
    public ReminderResult toggleComplete(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
        return reminderService.toggleComplete(id, memberId);
    }

    @PatchMapping("/api/reminders/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorder(@RequestBody ReorderReminderCommand command, @AuthenticationPrincipal Long memberId) {
        reminderService.reorder(command, memberId);
    }
}
