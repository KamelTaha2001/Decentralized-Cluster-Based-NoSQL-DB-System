package kamel.capstone.bootstrapnode.service;

import kamel.capstone.bootstrapnode.data.UsersDao;
import kamel.capstone.bootstrapnode.data.model.Node;
import kamel.capstone.bootstrapnode.data.model.User;
import kamel.capstone.bootstrapnode.data.model.UserRole;
import kamel.capstone.bootstrapnode.loadbalancer.UserLoadBalancer;
import kamel.capstone.bootstrapnode.util.Constants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@Service("default")
public class DefaultBootstrapNodeService implements BootstrapNodeService {
    private final Environment env;
    private final UsersDao usersDao;
    private final UserLoadBalancer userLoadBalancer;
    private final List<User> users;
    private final List<Node> nodes;
    private final int numberOfNodes;

    public DefaultBootstrapNodeService(
            Environment env,
            @Qualifier("storage") UsersDao usersDao,
            @Qualifier("round_robin") UserLoadBalancer userLoadBalancer
    ) {
        nodes = new LinkedList<>();
        users = new LinkedList<>();
        this.env = env;
        this.usersDao = usersDao;
        this.userLoadBalancer = userLoadBalancer;
        numberOfNodes = Integer.parseInt(env.getProperty("NUMBER_OF_NODES"));
        users.addAll(usersDao.getAllUsers());
    }

    @Override
    public boolean doesUserExist(String username) {
        return users.stream().map(User::getUsername).toList().contains(username);
    }

    @Override
    public void saveUser(User user) {
        if (usersDao.doesUserExist(user)) return;
        String nodeAddress = userLoadBalancer.distribute(users, nodes);
        user.setNodeAddress(nodeAddress);
        users.add(user);
        usersDao.saveUser(user);
    }

    @Override
    public Node registerNode(String nodeAddress) {
        Node node = new Node(nodeAddress);
        nodes.add(node);
        if (nodes.size() == numberOfNodes)
            addAdmin();
        return node;
    }

    @Override
    public Collection<Node> getNodes() {
        return nodes;
    }

    @Override
    public Collection<User> getUsers() {
        return users;
    }

    @Override
    public User getUser(String username) throws UserPrincipalNotFoundException {
        return usersDao.getUser(username);
    }

    @Override
    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    private void addAdmin() {
        User admin = new User();
        admin.setUsername(Constants.ADMIN_NAME);
        admin.setPassword(Constants.ADMIN_PASSWORD);
        admin.setRoles(List.of(UserRole.ADMIN));
        saveUser(admin);
    }
}
