package kamel.capstone.emaildemoapp.data.connector;

import kamel.capstone.emaildemoapp.model.CommandDTO;
import kamel.capstone.emaildemoapp.model.DatabaseUser;
import kamel.capstone.emaildemoapp.model.Result;
import kamel.capstone.emaildemoapp.model.UserDTO;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.*;

@Component("ConnectorImpl")
public class DatabaseConnectorImpl implements DatabaseConnector {
    private final RestTemplate restTemplate;
    private final Environment environment;
    private final String BOOTSTRAP_NODE_REGISTER_URL;
    private final String NOSQL_NODE_LOGIN_ENDPOINT;
    private final String NOSQL_NODE_COMMAND_ENDPOINT;
    private final String META_DATA_PATH;
    private final String USER_META_FILE_NAME = "user_meta";
    private DatabaseUser user;

    public DatabaseConnectorImpl(RestTemplate restTemplate, Environment environment) throws Exception {
        this.restTemplate = restTemplate;
        this.environment = environment;
        BOOTSTRAP_NODE_REGISTER_URL = environment.getProperty("BOOTSTRAP_NODE_REGISTER_URL");
        NOSQL_NODE_LOGIN_ENDPOINT = environment.getProperty("NOSQL_NODE_LOGIN_ENDPOINT");
        NOSQL_NODE_COMMAND_ENDPOINT = environment.getProperty("NOSQL_NODE_COMMAND_ENDPOINT");
        META_DATA_PATH = environment.getProperty("META_DATA_PATH");
        connect();
    }

    private void connect() throws IOException {
        String username = environment.getProperty("DATABASE_USERNAME");
        String password = environment.getProperty("DATABASE_PASSWORD");
        try {
            user = getUser();
            if (!user.getUsername().equals(username)) {
                throw new IOException();
            }
            UserDTO userDTO = new UserDTO(user.getUsername(), user.getPassword());
            String token = login(userDTO);
            user.setToken(token);
        } catch (ClassNotFoundException | IOException e) {
            UserDTO userDTO = new UserDTO(username, password);
            user = register(userDTO);
            user.setToken(login(userDTO));
        } finally {
            saveUser(user);
        }
    }

    private DatabaseUser getUser() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(META_DATA_PATH, USER_META_FILE_NAME)));
        DatabaseUser user = (DatabaseUser) ois.readObject();
        ois.close();
        if (user == null)
            throw new IOException();
        return user;
    }

    private void saveUser(DatabaseUser user) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(META_DATA_PATH, USER_META_FILE_NAME)));
        oos.writeObject(user);
        oos.close();
    }

    private DatabaseUser register(UserDTO userDTO) {
        ResponseEntity<DatabaseUser> response = restTemplate.postForEntity(BOOTSTRAP_NODE_REGISTER_URL, userDTO, DatabaseUser.class);
        return response.getBody();
    }

    private String login(UserDTO userDTO) {
        String url = user.getNodeAddress() + "/" + NOSQL_NODE_LOGIN_ENDPOINT;
        ResponseEntity<String> response = restTemplate.postForEntity(url, userDTO, String.class);
        return response.getBody();
    }

    @Override
    public Result sendCommand(String command) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", user.getToken());
        HttpEntity<CommandDTO> httpEntity = new HttpEntity<>(new CommandDTO(command), headers);
        String url = user.getNodeAddress() + "/" + NOSQL_NODE_COMMAND_ENDPOINT;
        ResponseEntity<Result> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Result.class);
        return response.getBody();
    }
}
