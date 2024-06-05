package kamel.capstone.nosqlnode.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.jsonwebtoken.MalformedJwtException;
import kamel.capstone.nosqlnode.data.broadcast.DataSynchronizer;
import kamel.capstone.nosqlnode.data.command.Command;
import kamel.capstone.nosqlnode.data.command.CommandType;
import kamel.capstone.nosqlnode.data.model.*;
import kamel.capstone.nosqlnode.data.command.CommandFactory;
import kamel.capstone.nosqlnode.security.JWT;
import kamel.capstone.nosqlnode.util.Constants;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service("NoSQLServiceImpl")
public class NoSQLNodeServiceImpl implements NoSQLNodeService {
    private final List<User> users;
    private final List<Node> otherNodes;
    private final CommandFactory commandFactory;
    private final JWT jwt;


    public NoSQLNodeServiceImpl(CommandFactory commandFactory, JWT jwt) {
        this.commandFactory = commandFactory;
        this.jwt = jwt;
        users = new LinkedList<>();
        otherNodes = new LinkedList<>();
    }

    @Override
    public List<User> getUsers() {
        return users;
    }

    @Override
    public void setUsers(List<User> users) {
        synchronized (this.users) {
            this.users.clear();
            this.users.addAll(users);
        }
    }

    @Override
    public List<Node> getOtherNodes() {
        return otherNodes;
    }

    @Override
    public void setOtherNodes(List<Node> otherNodes) {
        synchronized (this.otherNodes) {
            this.otherNodes.clear();
            this.otherNodes.addAll(otherNodes);
            DataSynchronizer.getInstance().addNodesAddresses(this.otherNodes.stream().map(Node::getNodeAddress).toList());
            System.out.println("Added " + otherNodes.stream().findFirst().get().getNodeAddress());
        }
    }

    @Override
    public void addUser(User user) {
        synchronized (this.users) {
            users.add(user);
        }
    }

    @Override
    public Result executeCommand(String command, List<UserRole> userRoles) {
        try {
            Command commandToExecute = commandFactory.createCommand(command, userRoles);
            if (commandToExecute.getType() == CommandType.MODIFYING_COMMAND)
                DataSynchronizer.getInstance().sendCommand(command, getOtherNodes().stream().map(Node::getNodeAddress).toList());
            return commandToExecute.execute();
        } catch (ClassNotFoundException | NoSuchMethodException | AccessDeniedException e) {
            return new Result(false, Collections.emptyList());
        }
    }

    @Override
    public Result executeBroadcastCommand(String command) {
        try {
            Command commandToExecute = commandFactory.createCommand(command, List.of(UserRole.ADMIN));
            return commandToExecute.execute();
        } catch (NoSuchMethodException | ClassNotFoundException | AccessDeniedException e) {
            return new Result(false, Collections.emptyList());
        }
    }

    @Override
    public User authenticate(String token) throws AccessDeniedException {
        synchronized (this.users) {
            try {
                String username = jwt.resolveClaims(token).getSubject();
                Optional<User> userOptional = users.stream().filter(user -> user.getUsername().equals(username)).findFirst();
                if (userOptional.isPresent()) {
                    if (userOptional.get().getToken().equals(token))
                        return userOptional.get();
                }
                throw new AccessDeniedException("Authentication process failed.");
            } catch (MalformedJwtException e) {
                throw new AccessDeniedException("Authentication process failed.");
            }
        }
    }

    @Override
    public String authenticate(UserDTO userDTO) throws AccessDeniedException {
        synchronized (this.users) {
            Optional<User> userOptional = users.stream().filter(user -> user.getUsername().equals(userDTO.getUsername())).findFirst();
            if (userOptional.isPresent()) {
                if (userOptional.get().getPassword().equals(userDTO.getPassword())) {
                    String token = jwt.createToken(userOptional.get());
                    List<User> usersCopy = new LinkedList<>(users.stream().peek(user -> {
                        if (user.getUsername().equals(userDTO.getUsername()))
                            user.setToken(token);
                    }).toList());
                    users.clear();
                    users.addAll(usersCopy);
                    return token;
                }
            }
            throw new AccessDeniedException("Authentication process failed.");
        }
    }

    @Override
    public Stats getStats() {
        Stats stats = new Stats();
        String thisNodeAddress = DataSynchronizer.getInstance().getThisNodeAddress();
        stats.setNodeAddress(thisNodeAddress);

        List<String> usernames = getUsers().stream().map(User::getUsername).toList();
        stats.setUsernames(usernames);

        List<String> collectionsNames = executeCommand("DESCRIBE()", List.of(UserRole.ADMIN)).getResult();
        Gson gson = new GsonBuilder().create();
        List<String> documentIDs = new LinkedList<>();
        collectionsNames.forEach(name -> {
             Result result = executeCommand(
                     "SELECT_FROM("
                     + name
                     + ") ATTRIBUTES("
                     + Constants.ID_ATTRIBUTE_NAME
                     + ") WHERE("
                     + Constants.AFFINITY_ATTRIBUTE_NAME
                     + "=" + thisNodeAddress
                     + ")",
                     List.of(UserRole.ADMIN)
             );
             documentIDs.addAll(
                     result.getResult()
                             .stream()
                             .filter(id -> !id.equals("0"))
                             .map(r -> name + ": " + gson.fromJson(r, IDDTO.class).toString())
                             .toList()
             );
        });
        stats.setDocumentIDs(documentIDs);
        return stats;
    }
}
