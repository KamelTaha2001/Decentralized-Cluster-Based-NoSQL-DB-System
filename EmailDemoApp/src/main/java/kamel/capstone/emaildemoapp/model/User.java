package kamel.capstone.emaildemoapp.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    @JsonProperty
    private String username;
    @JsonProperty
    private String password;
    @JsonProperty
    private String token;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
