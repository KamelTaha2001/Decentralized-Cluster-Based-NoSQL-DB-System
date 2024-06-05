package kamel.capstone.bootstrapnode.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Node {
    @JsonProperty
    private String nodeAddress;

    public Node() {}

    public Node(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (!(obj instanceof Node)) return false;

        return nodeAddress.equals(((Node) obj).nodeAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeAddress);
    }
}
