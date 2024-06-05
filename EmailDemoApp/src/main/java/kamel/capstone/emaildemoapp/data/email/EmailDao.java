package kamel.capstone.emaildemoapp.data.email;

import kamel.capstone.emaildemoapp.model.Email;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface EmailDao {
    List<Email> getEmailsByUsername(String username);
    boolean compose(Email email);
}
