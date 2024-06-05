package kamel.capstone.emaildemoapp.data.connector;

import kamel.capstone.emaildemoapp.model.Result;
import org.springframework.stereotype.Component;

@Component
public interface DatabaseConnector {
    Result sendCommand(String command);
}
