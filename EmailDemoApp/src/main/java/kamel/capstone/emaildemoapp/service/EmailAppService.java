package kamel.capstone.emaildemoapp.service;

import kamel.capstone.emaildemoapp.model.Email;
import kamel.capstone.emaildemoapp.model.User;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;

@Service
public interface EmailAppService {
    boolean registerUser(User user);
    String login(User user);
    List<Email> getEmailsByUsername(String username);
    List<Email> getEmailsByToken(String token) throws UserPrincipalNotFoundException;
    User getUserByToken(String token) throws UserPrincipalNotFoundException;
    boolean compose(Email email);
}
