package bamdul.ai.reminder.service.port.in;

import bamdul.ai.reminder.service.dto.AuthResult;
import bamdul.ai.reminder.service.dto.LoginCommand;
import bamdul.ai.reminder.service.dto.SignupCommand;

public interface AuthService {

    AuthResult signup(SignupCommand command);

    AuthResult login(LoginCommand command);
}
