package kamel.capstone.nosqlnode.service;

import kamel.capstone.nosqlnode.data.model.*;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public interface NoSQLNodeService {
    List<User> getUsers();

    void setUsers(List<User> users);

    List<Node> getOtherNodes();

    void setOtherNodes(List<Node> otherNodes);

    void addUser(User user);

    Result executeCommand(String command, List<UserRole> userRoles);

    Result executeBroadcastCommand(String command);

    User authenticate(String token) throws AccessDeniedException;

    String authenticate(UserDTO userDTO) throws AccessDeniedException;

    Stats getStats();
}
