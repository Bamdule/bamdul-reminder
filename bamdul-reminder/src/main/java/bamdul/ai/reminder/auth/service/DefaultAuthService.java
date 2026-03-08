package bamdul.ai.reminder.auth.service;

import bamdul.ai.reminder.auth.domain.Member;
import bamdul.ai.reminder.auth.exception.DuplicateEmailException;
import bamdul.ai.reminder.auth.repository.MemberRepository;
import bamdul.ai.reminder.auth.security.JwtTokenProvider;
import bamdul.ai.reminder.auth.service.dto.AuthResult;
import bamdul.ai.reminder.auth.service.dto.LoginCommand;
import bamdul.ai.reminder.auth.service.dto.MemberResult;
import bamdul.ai.reminder.auth.service.dto.SignupCommand;
import bamdul.ai.reminder.auth.service.port.in.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultAuthService implements AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResult signup(SignupCommand command) {
        if (memberRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException(command.email());
        }

        Member member = Member.builder()
                .email(command.email())
                .password(passwordEncoder.encode(command.password()))
                .name(command.name())
                .build();

        member = memberRepository.save(member);
        String token = jwtTokenProvider.generateToken(member.getId());

        return new AuthResult(token, MemberResult.from(member));
    }

    @Override
    public AuthResult login(LoginCommand command) {
        Member member = memberRepository.findByEmail(command.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(command.password(), member.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtTokenProvider.generateToken(member.getId());
        return new AuthResult(token, MemberResult.from(member));
    }
}
