package kamel.capstone.bootstrapnode.service;

import kamel.capstone.bootstrapnode.data.model.Node;
import kamel.capstone.bootstrapnode.data.model.User;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Collection;

@Service
public interface BootstrapNodeService {
    boolean doesUserExist(String username);
    void saveUser(User user);
    Node registerNode(String nodeAddress);
    Collection<Node> getNodes();
    Collection<User> getUsers();
    User getUser(String username) throws UserPrincipalNotFoundException;
    int getNumberOfNodes();
}
