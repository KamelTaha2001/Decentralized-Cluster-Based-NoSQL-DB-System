package kamel.capstone.emaildemoapp.service;

import kamel.capstone.emaildemoapp.data.email.EmailDao;
import kamel.capstone.emaildemoapp.data.user.UserDao;
import kamel.capstone.emaildemoapp.model.Email;
import kamel.capstone.emaildemoapp.model.User;
import kamel.capstone.emaildemoapp.security.JWT;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;

@Service("ServiceImpl")
public class EmailAppServiceImpl implements EmailAppService {
    private final UserDao userDao;
    private final EmailDao emailDao;
    private final JWT jwt;

    public EmailAppServiceImpl(UserDao userDao, EmailDao emailDao, JWT jwt) {
        this.userDao = userDao;
        this.emailDao = emailDao;
        this.jwt = jwt;
    }

    @Override
    public boolean registerUser(User user) {
        synchronized (userDao) {
            try {
                userDao.getUser(user.getUsername());
                return false;
            } catch (UserPrincipalNotFoundException e) {
                return userDao.addUser(user);
            }
        }
    }

    @Override
    public String login(User user) {
        synchronized (userDao) {
            try {
                User foundUser = userDao.getUser(user.getUsername());
                String token = jwt.createToken(foundUser);
                userDao.setToken(user.getUsername(), token);
                return token;
            } catch (UserPrincipalNotFoundException e) {
                return "";
            }
        }
    }

    @Override
    public List<Email> getEmailsByUsername(String username) {
        return emailDao.getEmailsByUsername(username);
    }

    @Override
    public List<Email> getEmailsByToken(String token) throws UserPrincipalNotFoundException {
        User user = userDao.getUserByToken(token);
        return emailDao.getEmailsByUsername(user.getUsername());
    }

    @Override
    public User getUserByToken(String token) throws UserPrincipalNotFoundException {
        return userDao.getUserByToken(token);
    }

    @Override
    public boolean compose(Email email) {
        try {
            userDao.getUser(email.getReceiver());
            return emailDao.compose(email);
        } catch (UserPrincipalNotFoundException e) {
            return false;
        }
    }
}
