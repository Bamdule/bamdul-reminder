package bamdul.ai.reminder.controller;

import bamdul.ai.reminder.service.dto.CreateReminderListCommand;
import bamdul.ai.reminder.service.dto.ReminderListResult;
import bamdul.ai.reminder.service.dto.ReorderCommand;
import bamdul.ai.reminder.service.dto.UpdateReminderListCommand;
import bamdul.ai.reminder.service.port.in.ReminderListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class ReminderListController {

    private final ReminderListService reminderListService;

    @GetMapping
    public List<ReminderListResult> findAll(@AuthenticationPrincipal Long memberId) {
        return reminderListService.findAll(memberId);
    }

    @GetMapping("/{id}")
    public ReminderListResult findById(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
        return reminderListService.findById(id, memberId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReminderListResult create(@RequestBody CreateReminderListCommand command, @AuthenticationPrincipal Long memberId) {
        return reminderListService.create(command, memberId);
    }

    @PutMapping("/{id}")
    public ReminderListResult update(@PathVariable Long id, @RequestBody UpdateReminderListCommand command, @AuthenticationPrincipal Long memberId) {
        return reminderListService.update(id, command, memberId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @AuthenticationPrincipal Long memberId) {
        reminderListService.delete(id, memberId);
    }

    @PatchMapping("/reorder")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorder(@RequestBody ReorderCommand command, @AuthenticationPrincipal Long memberId) {
        reminderListService.reorder(command, memberId);
    }
}
