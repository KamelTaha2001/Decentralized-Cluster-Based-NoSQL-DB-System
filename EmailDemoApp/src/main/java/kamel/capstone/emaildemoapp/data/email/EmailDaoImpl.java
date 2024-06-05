package kamel.capstone.emaildemoapp.data.email;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kamel.capstone.emaildemoapp.data.connector.DatabaseConnector;
import kamel.capstone.emaildemoapp.model.Email;
import kamel.capstone.emaildemoapp.model.Result;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("EmailDaoImpl")
public class EmailDaoImpl implements EmailDao {
    private final DatabaseConnector connector;
    private final String COLLECTION_NAME = "Emails";

    public EmailDaoImpl(@Qualifier("ConnectorImpl") DatabaseConnector connector) {
        this.connector = connector;
        createCollection();
    }

    private void createCollection() {
        connector.sendCommand(
                "CREATE_COLLECTION("
                + COLLECTION_NAME
                + ") ATTRIBUTES(sender:string, receiver:string, subject:string, message:string)"
        );
    }

    @Override
    public List<Email> getEmailsByUsername(String username) {
        Result result = connector.sendCommand(
                "SELECT_FROM("
                + COLLECTION_NAME
                + ") ATTRIBUTES(sender, receiver, subject, message) WHERE(receiver="
                + username
                + ")"
        );
        Gson gson = new GsonBuilder().create();
        return result.getResult().stream()
                .map(json -> gson.fromJson(json, Email.class))
                .toList();
    }

    @Override
    public boolean compose(Email email) {
        Result result = connector.sendCommand(
                "INSERT_INTO("
                + COLLECTION_NAME
                + ") ATTRIBUTES("
                + "sender=" + email.getSender()
                + ", receiver=" + email.getReceiver()
                + ", subject=" + email.getSubject()
                + ", message=" + email.getMessage()
                + ")"
        );
        return result.isSuccess();
    }
}
