package kamel.capstone.nosqlnode.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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
}
