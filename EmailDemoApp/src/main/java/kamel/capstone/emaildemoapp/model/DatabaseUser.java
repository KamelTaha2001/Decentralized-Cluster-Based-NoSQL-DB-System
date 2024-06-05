package kamel.capstone.emaildemoapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class DatabaseUser implements Serializable {
    @JsonProperty
    private String username;
    @JsonProperty
    private String password;
    @JsonProperty
    private List<DatabaseUserRole> roles;
    @JsonProperty
    private String nodeAddress;
    @JsonProperty
    private String token;

    public DatabaseUser() {}

    public DatabaseUser(String username, String password, List<DatabaseUserRole> roles) {
        this.username = username;
        this.roles = roles;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<DatabaseUserRole> getRoles() {
        return roles;
    }

    public void setRoles(List<DatabaseUserRole> roles) {
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "DatabaseUser{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", roles=" + roles +
                ", nodeAddress='" + nodeAddress + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
