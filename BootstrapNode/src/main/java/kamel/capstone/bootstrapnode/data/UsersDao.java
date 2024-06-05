package kamel.capstone.bootstrapnode.data;

import kamel.capstone.bootstrapnode.data.model.User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Collection;

@Component
public interface UsersDao {
    Collection<User> getAllUsers();
    User getUser(String username) throws UserPrincipalNotFoundException;
    void saveUser(User user);
    boolean doesUserExist(User user);
}
