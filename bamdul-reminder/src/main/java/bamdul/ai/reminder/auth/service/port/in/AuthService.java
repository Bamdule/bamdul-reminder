package bamdul.ai.reminder.auth.service.port.in;

import bamdul.ai.reminder.auth.service.dto.AuthResult;
import bamdul.ai.reminder.auth.service.dto.LoginCommand;
import bamdul.ai.reminder.auth.service.dto.SignupCommand;

public interface AuthService {

    AuthResult signup(SignupCommand command);

    AuthResult login(LoginCommand command);
}
