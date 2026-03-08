package bamdul.ai.reminder.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

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
}
