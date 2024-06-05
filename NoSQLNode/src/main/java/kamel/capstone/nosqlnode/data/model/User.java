package kamel.capstone.nosqlnode.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class User implements Serializable {
    @JsonProperty
    private String username;
    @JsonProperty
    private String nodeAddress = "";
    @JsonProperty
    private String password = "";
    @JsonProperty
    private String token = "";
    @JsonProperty
    private List<UserRole> roles;

    public User() {
    }

    public User(String username, List<UserRole> roles) {
        this.username = username;
        this.roles = roles;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNodeAddress() {
        return this.nodeAddress;
    }

    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (!(obj instanceof User)) return false;

        return this.username.equals(((User) obj).username);
    }

    public int hashCode() {
        return Objects.hash(username);
    }

    public String toString() {
        return this.username + ": " + this.token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<UserRole> getRoles() {
        return roles;
    }

    public void setRoles(List<UserRole> roles) {
        this.roles = roles;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}

