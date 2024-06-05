package kamel.capstone.bootstrapnode.controller;

import kamel.capstone.bootstrapnode.data.model.Node;
import kamel.capstone.bootstrapnode.data.model.User;
import kamel.capstone.bootstrapnode.data.model.UserDTO;
import kamel.capstone.bootstrapnode.data.model.UserRole;
import kamel.capstone.bootstrapnode.service.BootstrapNodeService;
import kamel.capstone.bootstrapnode.util.Constants;
import kamel.capstone.bootstrapnode.util.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/")
public class BootstrapNodeController {
    private final BootstrapNodeService service;
    private final RestTemplate template;
    private final Environment env;

    public BootstrapNodeController(
            @Qualifier("default") BootstrapNodeService service,
            RestTemplate template,
            Environment env
    ) {
        this.service = service;
        this.template = template;
        this.env = env;
    }

    @PostMapping("/register-user")
    public ResponseEntity<User> registerUser(@RequestBody UserDTO userDTO) {
        synchronized (service.getUsers()) {
            try {
                return ResponseEntity.ok(service.getUser(userDTO.getUsername()));
            } catch (UserPrincipalNotFoundException e) {
                User user = new User(userDTO.getUsername(), userDTO.getPassword(), List.of(UserRole.USER));
                service.saveUser(user);
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "atypon_training");
                HttpEntity<Object> requestEntity = new HttpEntity<>(user, headers);
                template.exchange(user.getNodeAddress() + "/bootstrap/add-user", HttpMethod.POST, requestEntity, User.class);
                return ResponseEntity.ok(user);
            }
        }
    }

    @PostMapping("/nosql/register-node")
    public ResponseEntity<String> registerNode(@RequestBody String nodeAddress) {
        synchronized (service.getNodes()) {
            service.registerNode(nodeAddress);
            if (service.getNodes().size() == service.getNumberOfNodes()) {
                Collection<Node> noSQLNodes = service.getNodes();
                for (Node noSQLNode : noSQLNodes) {
                    Collection<Node> otherNodes =
                            noSQLNodes.stream()
                                    .filter(n -> !n.equals(noSQLNode))
                                    .toList();
                    HttpHeaders headers = new HttpHeaders();
                    headers.set("Authorization", Constants.PRIVATE_KEY);
                    HttpEntity<Collection<Node>> nodesRequestEntity = new HttpEntity<>(otherNodes, headers);
                    template.exchange(noSQLNode.getNodeAddress() + "/bootstrap/nodes-addresses", HttpMethod.POST, nodesRequestEntity, String.class);
                    Collection<User> nodeUsers =
                            service.getUsers()
                                    .stream()
                                    .filter(u -> u.getNodeAddress().equals(noSQLNode.getNodeAddress()))
                                    .toList();
                    HttpEntity<Collection<User>> usersRequestEntity = new HttpEntity<>(nodeUsers, headers);
                    template.exchange(noSQLNode.getNodeAddress() + "/bootstrap/node-users", HttpMethod.POST, usersRequestEntity, String.class);
                }
            }
            return ResponseEntity.ok("");
        }
    }
}
