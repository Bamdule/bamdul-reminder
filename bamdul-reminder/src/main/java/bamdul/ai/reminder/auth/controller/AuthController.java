package bamdul.ai.reminder.auth.controller;

import bamdul.ai.reminder.auth.service.dto.AuthResult;
import bamdul.ai.reminder.auth.service.dto.LoginCommand;
import bamdul.ai.reminder.auth.service.dto.SignupCommand;
import bamdul.ai.reminder.auth.service.port.in.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResult signup(@Valid @RequestBody SignupCommand command) {
        return authService.signup(command);
    }

    @PostMapping("/login")
    public AuthResult login(@RequestBody LoginCommand command) {
        return authService.login(command);
    }
}
