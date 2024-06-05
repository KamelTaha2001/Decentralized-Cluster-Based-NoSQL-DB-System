package kamel.capstone.bootstrapnode.node;

import kamel.capstone.bootstrapnode.data.model.Node;
import kamel.capstone.bootstrapnode.data.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface BootstrapNode {
    ResponseEntity<String> registerUser(@RequestBody User user);
    ResponseEntity<Node> registerNode();
}
