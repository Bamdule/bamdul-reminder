package bamdul.ai.reminder.controller;

import bamdul.ai.reminder.repository.MemberRepository;
import bamdul.ai.reminder.service.dto.SignupCommand;
import bamdul.ai.reminder.service.port.in.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/auth/signup")
    class SignupTest {

        @Test
        @DisplayName("회원가입에 성공하면 201과 토큰을 반환한다")
        void signup() throws Exception {
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@example.com", "password": "password123", "name": "홍길동"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token", notNullValue()))
                    .andExpect(jsonPath("$.member.email", is("test@example.com")))
                    .andExpect(jsonPath("$.member.name", is("홍길동")));
        }

        @Test
        @DisplayName("이메일 중복 시 409를 반환한다")
        void signupDuplicate() throws Exception {
            authService.signup(new SignupCommand("test@example.com", "password123", "홍길동"));

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@example.com", "password": "password456", "name": "김철수"}
                                    """))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTest {

        @Test
        @DisplayName("올바른 자격증명으로 로그인하면 200과 토큰을 반환한다")
        void login() throws Exception {
            authService.signup(new SignupCommand("test@example.com", "password123", "홍길동"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@example.com", "password": "password123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", notNullValue()))
                    .andExpect(jsonPath("$.member.email", is("test@example.com")));
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 401을 반환한다")
        void loginInvalidPassword() throws Exception {
            authService.signup(new SignupCommand("test@example.com", "password123", "홍길동"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "test@example.com", "password": "wrongpassword"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }
    }
}
