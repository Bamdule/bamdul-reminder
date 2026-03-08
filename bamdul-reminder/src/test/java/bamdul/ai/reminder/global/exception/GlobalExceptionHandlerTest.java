package bamdul.ai.reminder.global.exception;

import bamdul.ai.reminder.auth.exception.DuplicateEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("ResourceNotFoundException 발생 시 404 ProblemDetail을 반환한다")
    void handleResourceNotFound() {
        var ex = new ResourceNotFoundException("ReminderList", 999L);

        var result = handler.handleResourceNotFound(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getDetail()).contains("ReminderList").contains("999");
    }

    @Test
    @DisplayName("DuplicateEmailException 발생 시 409 ProblemDetail을 반환한다")
    void handleDuplicateEmail() {
        var ex = new DuplicateEmailException("test@example.com");

        var result = handler.handleDuplicateEmail(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getDetail()).contains("test@example.com");
    }

    @Test
    @DisplayName("BadCredentialsException 발생 시 401 ProblemDetail을 반환한다")
    void handleBadCredentials() {
        var ex = new BadCredentialsException("Invalid email or password");

        var result = handler.handleBadCredentials(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
