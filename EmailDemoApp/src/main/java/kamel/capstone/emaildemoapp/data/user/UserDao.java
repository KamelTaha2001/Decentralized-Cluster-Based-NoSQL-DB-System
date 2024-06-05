package kamel.capstone.emaildemoapp.data.user;

import kamel.capstone.emaildemoapp.model.User;
import org.springframework.stereotype.Component;

import java.nio.file.attribute.UserPrincipalNotFoundException;


@Component
public interface UserDao {
    boolean addUser(User user);
    User getUser(String username) throws UserPrincipalNotFoundException;
    User getUserByToken(String token) throws UserPrincipalNotFoundException;
    boolean setToken(String username, String token);
}
