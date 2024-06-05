package kamel.capstone.bootstrapnode.data;


import kamel.capstone.bootstrapnode.data.model.User;
import kamel.capstone.bootstrapnode.util.Constants;
import kamel.capstone.bootstrapnode.util.IOUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.io.File;
import java.io.IOException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component("storage")
public class StorageUsersDao implements UsersDao {
    private final String BOOTSTRAP_META_PATH;
    private final String USERS_FILE_NAME = "users";
    private final File usersFile;
    private final Environment environment;


    public StorageUsersDao(Environment environment) {
        this.environment = environment;
        BOOTSTRAP_META_PATH = environment.getProperty("BOOTSTRAP_META_PATH");
        File directory = new File(BOOTSTRAP_META_PATH);
        if (!directory.exists()) {
            directory.mkdir();
        }
        usersFile = new File(BOOTSTRAP_META_PATH, USERS_FILE_NAME);
        try {
            if (!usersFile.exists()) {
                usersFile.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot find or create users file!");
        } catch (SecurityException e) {
            throw new RuntimeException("Cannot find or create users file! Please check the permissions of the file or the folder containing the file!");
        }
    }


    @Override
    public Collection<User> getAllUsers() {
        List<User> users = IOUtils.readObjectsFromFile(usersFile);
        System.out.println("All users: " + users);
        return users;
    }

    @Override
    public User getUser(String username) throws UserPrincipalNotFoundException {
        Collection<User> users = getAllUsers();
        Optional<User> user = users.stream().filter(u -> u.getUsername().equals(username)).findFirst();
        if (user.isPresent()) {
            return user.get();
        }
        throw new UserPrincipalNotFoundException("User " + username + " not found.");
    }

    @Override
    public void saveUser(User user) {
        IOUtils.saveObjectInFile(usersFile, user);
    }

    @Override
    public boolean doesUserExist(User user) {
        return IOUtils.doesObjectExistInFile(usersFile, user);
    }
}
