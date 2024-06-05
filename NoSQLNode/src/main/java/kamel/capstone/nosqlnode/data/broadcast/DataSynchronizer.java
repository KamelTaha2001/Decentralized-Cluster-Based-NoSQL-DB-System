package kamel.capstone.nosqlnode.data.broadcast;

import kamel.capstone.nosqlnode.data.model.CommandDTO;
import kamel.capstone.nosqlnode.util.Constants;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class DataSynchronizer {
    private final List<String> nodesAddresses;
    private String thisNodeAddress;
    private final RestTemplate restTemplate;

    private static class DataSynchronizerHolder {
        static final DataSynchronizer INSTANCE = new DataSynchronizer();
    }

    public static DataSynchronizer getInstance() {
        return DataSynchronizerHolder.INSTANCE;
    }

    private DataSynchronizer() {
        restTemplate = new RestTemplate();
        nodesAddresses = new LinkedList<>();
    }

    public List<String> getNodesAddresses() {
        return nodesAddresses;
    }

    public void addNodesAddresses(List<String> addresses) {
        synchronized (this.nodesAddresses) {
            nodesAddresses.addAll(addresses);
        }
    }

    public void setThisNodeAddress(String thisNodeAddress) {
        this.thisNodeAddress = thisNodeAddress;
        synchronized (this.nodesAddresses) {
            if (!nodesAddresses.contains(thisNodeAddress))
                nodesAddresses.add(thisNodeAddress);
        }
    }

    public void broadcastData(DataSyncingAction syncingAction) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", Constants.PRIVATE_KEY);
        HttpEntity<DataSyncingAction> entity = new HttpEntity<>(syncingAction, headers);
        for (String address : getOtherNodes()) {
            restTemplate.exchange(address + Constants.BROADCAST_ENDPOINT, HttpMethod.POST, entity, String.class);
        }
    }

    public void sendCommand(String command, String nodeAddress) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", Constants.PRIVATE_KEY);
        CommandDTO commandDTO = new CommandDTO(command);
        HttpEntity<CommandDTO> requestEntity = new HttpEntity<>(commandDTO, headers);
        restTemplate.exchange(nodeAddress + Constants.COMMAND_ENDPOINT, HttpMethod.POST, requestEntity, String.class);
    }

    public void sendCommand(String command, List<String> nodeAddresses) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", Constants.PRIVATE_KEY);
        CommandDTO commandDTO = new CommandDTO(command);
        HttpEntity<CommandDTO> requestEntity = new HttpEntity<>(commandDTO, headers);
        for (String nodeAddress : nodeAddresses) {
            restTemplate.exchange(nodeAddress + Constants.COMMAND_ENDPOINT, HttpMethod.POST, requestEntity, String.class);
        }
    }

    public String getThisNodeAddress() {
        return thisNodeAddress;
    }

    public List<String> getOtherNodes() {
        return new LinkedList<>(
                nodesAddresses.stream().filter(address -> !address.equals(thisNodeAddress)).toList()
        );
    }
}
