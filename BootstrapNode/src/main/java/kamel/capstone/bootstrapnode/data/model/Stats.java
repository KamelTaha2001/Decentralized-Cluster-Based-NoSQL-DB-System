package kamel.capstone.bootstrapnode.data.model;

import java.util.List;

public class Stats {
    private String nodeAddress;
    private List<String> usernames;
    private List<String> documentIDs;

    public Stats() {
    }

    public Stats(String nodeAddress, List<String> usernames, List<String> documentIDs) {
        this.nodeAddress = nodeAddress;
        this.usernames = usernames;
        this.documentIDs = documentIDs;
    }

    public List<String> getUsernames() {
        return usernames;
    }

    public void setUsernames(List<String> usernames) {
        this.usernames = usernames;
    }

    public List<String> getDocumentIDs() {
        return documentIDs;
    }

    public void setDocumentIDs(List<String> documentIDs) {
        this.documentIDs = documentIDs;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }
}

