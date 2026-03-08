package bamdul.ai.reminder.service;

import bamdul.ai.reminder.exception.DuplicateEmailException;
import bamdul.ai.reminder.repository.MemberRepository;
import bamdul.ai.reminder.service.dto.AuthResult;
import bamdul.ai.reminder.service.dto.LoginCommand;
import bamdul.ai.reminder.service.dto.SignupCommand;
import bamdul.ai.reminder.service.port.in.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("signup")
    class SignupTest {

        @Test
        @DisplayName("회원가입에 성공하면 토큰과 회원 정보를 반환한다")
        void signup() {
            var command = new SignupCommand("test@example.com", "password123", "홍길동");

            AuthResult result = authService.signup(command);

            assertThat(result.token()).isNotBlank();
            assertThat(result.member().email()).isEqualTo("test@example.com");
            assertThat(result.member().name()).isEqualTo("홍길동");
            assertThat(result.member().id()).isNotNull();
            assertThat(result.member().createdAt()).isNotNull();
        }

        @Test
        @DisplayName("비밀번호는 BCrypt로 암호화되어 저장된다")
        void signupEncryptsPassword() {
            var command = new SignupCommand("test@example.com", "password123", "홍길동");

            authService.signup(command);

            var member = memberRepository.findByEmail("test@example.com").orElseThrow();
            assertThat(member.getPassword()).isNotEqualTo("password123");
            assertThat(member.getPassword()).startsWith("$2");
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 가입 시 예외가 발생한다")
        void signupDuplicateEmail() {
            authService.signup(new SignupCommand("test@example.com", "password123", "홍길동"));

            assertThatThrownBy(() -> authService.signup(new SignupCommand("test@example.com", "password456", "김철수")))
                    .isInstanceOf(DuplicateEmailException.class);
        }
    }

    @Nested
    @DisplayName("login")
    class LoginTest {

        @Test
        @DisplayName("올바른 이메일과 비밀번호로 로그인하면 토큰을 반환한다")
        void login() {
            authService.signup(new SignupCommand("test@example.com", "password123", "홍길동"));

            AuthResult result = authService.login(new LoginCommand("test@example.com", "password123"));

            assertThat(result.token()).isNotBlank();
            assertThat(result.member().email()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
        void loginInvalidEmail() {
            assertThatThrownBy(() -> authService.login(new LoginCommand("nonexist@example.com", "password123")))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 예외가 발생한다")
        void loginInvalidPassword() {
            authService.signup(new SignupCommand("test@example.com", "password123", "홍길동"));

            assertThatThrownBy(() -> authService.login(new LoginCommand("test@example.com", "wrongpassword")))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }
}
