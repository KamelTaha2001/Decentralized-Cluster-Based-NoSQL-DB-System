package kamel.capstone.emaildemoapp.data.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kamel.capstone.emaildemoapp.data.connector.DatabaseConnector;
import kamel.capstone.emaildemoapp.model.Result;
import kamel.capstone.emaildemoapp.model.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.attribute.UserPrincipalNotFoundException;

@Component("UserDaoImpl")
public class UserDaoImpl implements UserDao {
    private final DatabaseConnector connector;
    private final String COLLECTION_NAME = "EmailAppUsers";

    public UserDaoImpl(@Qualifier("ConnectorImpl") DatabaseConnector connector) {
        this.connector = connector;
        createCollection();
    }

    private void createCollection() {
         connector.sendCommand(
                "CREATE_COLLECTION("
                        + COLLECTION_NAME
                        + ") ATTRIBUTES(username:string, password:string, token:string)"
        );
    }


    @Override
    public boolean addUser(User user) {
        Result result = connector.sendCommand(
                "INSERT_INTO("
                + COLLECTION_NAME
                + ") ATTRIBUTES("
                + "username=" + user.getUsername()
                + ", password=" + user.getPassword()
                + ")"
        );
        return result.isSuccess();
    }

    @Override
    public User getUser(String username) throws UserPrincipalNotFoundException {
        Result result = connector.sendCommand(
                "SELECT_FROM("
                + COLLECTION_NAME
                + ") ATTRIBUTES(username, password, token) WHERE("
                + "username=" + username
                + ")"
        );
        if (result.getResult() == null || result.getResult().isEmpty())
            throw new UserPrincipalNotFoundException("User \"" + username + "\" was not found.");
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(result.getResult().get(0), User.class);
    }

    @Override
    public User getUserByToken(String token) throws UserPrincipalNotFoundException {
        Result result = connector.sendCommand(
                "SELECT_FROM("
                        + COLLECTION_NAME
                        + ") ATTRIBUTES(username, password, token) WHERE("
                        + "token=" + token
                        + ")"
        );
        if (result.getResult() == null || result.getResult().isEmpty())
            throw new UserPrincipalNotFoundException("User was not found.");
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(result.getResult().get(0), User.class);
    }

    @Override
    public boolean setToken(String username, String token) {
        Result result = connector.sendCommand(
                "UPDATE("
                + COLLECTION_NAME
                + ") ATTRIBUTES("
                + "token=" + token
                + ") WHERE(username=" + username
                + ")"
        );
        return result.isSuccess();
    }
}
