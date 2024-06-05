package kamel.capstone.nosqlnode;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kamel.capstone.nosqlnode.data.broadcast.DataSynchronizer;
import kamel.capstone.nosqlnode.data.model.Node;
import kamel.capstone.nosqlnode.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class NoSQLNodeApplication implements ApplicationRunner {
    @Autowired
    private RestTemplate template;
    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(NoSQLNodeApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        String bootstrapNodeAddress = environment.getProperty("BOOTSTRAP_NODE_ADDRESS");
        String thisNodeAddress = getContainerAddress();
        DataSynchronizer.getInstance().setThisNodeAddress(thisNodeAddress);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", Constants.PRIVATE_KEY);
        HttpEntity<String> entity = new HttpEntity<>(thisNodeAddress, headers);
        template.exchange(bootstrapNodeAddress + "/nosql/register-node", HttpMethod.POST, entity, String.class);
    }

    private String getContainerAddress() throws IOException {
        String containerId = environment.getProperty("HOSTNAME");
        String apiUrl = "http://socat:2375/containers/" + containerId + "/json";
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(response.toString());
        String address = node.get("Name").asText();
        connection.disconnect();
        return "http:/" + address + ":8081";
    }
}
