package kamel.capstone.bootstrapnode.loadbalancer;

import kamel.capstone.bootstrapnode.data.model.Node;
import kamel.capstone.bootstrapnode.data.model.User;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public interface UserLoadBalancer {
    String distribute(Collection<User> existingUsers, Collection<Node> nodes);
}
