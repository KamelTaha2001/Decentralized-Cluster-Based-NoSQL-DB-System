package kamel.capstone.bootstrapnode.loadbalancer;

import kamel.capstone.bootstrapnode.data.model.Node;
import kamel.capstone.bootstrapnode.data.model.User;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("round_robin")
public class RoundRobinLoadBalancer implements UserLoadBalancer {
    @Override
    public String distribute(Collection<User> existingUsers, Collection<Node> nodes) {
        if (existingUsers.size() < nodes.size()) {
            List<Node> nonUsedNodes =
                    nodes.stream().filter(node -> {
                        boolean exists = existingUsers
                        .stream()
                        .map(User::getNodeAddress)
                        .toList()
                        .contains(node.getNodeAddress());
                        return !exists;
            }).toList();
            return nonUsedNodes.get(0).getNodeAddress();
        }

        Map<String, Long> countForEachNode =
                existingUsers.stream()
                        .collect(Collectors.groupingBy(User::getNodeAddress, Collectors.counting()));

        Optional<Map.Entry<String, Long>> leastEntry = countForEachNode.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue());
        if (leastEntry.isPresent())
            return leastEntry.get().getKey();
        else
            return nodes.stream().toList().get(0).getNodeAddress();
    }
}
