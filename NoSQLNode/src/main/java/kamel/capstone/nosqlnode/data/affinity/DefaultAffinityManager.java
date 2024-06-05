package kamel.capstone.nosqlnode.data.affinity;

import kamel.capstone.nosqlnode.data.broadcast.DataSynchronizer;
import kamel.capstone.nosqlnode.data.broadcast.WriteFileSyncAction;
import kamel.capstone.nosqlnode.data.model.Document;
import kamel.capstone.nosqlnode.util.Constants;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultAffinityManager implements AffinityManager {
    private File nodesLoadPath;

    public DefaultAffinityManager() {
        this.nodesLoadPath = new File(Constants.getAffinityFilePath());
    }

    private Map<String, Long> load() {
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(nodesLoadPath))) {
            return (HashMap<String, Long>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return new HashMap<>();
        }
    }

    private void save(Map<String, Long> nodesLoad) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nodesLoadPath))) {
            oos.writeObject(nodesLoad);
            FileInputStream fis = new FileInputStream(nodesLoadPath);
            fis.close();
        } catch (IOException e) {}
    }

    @Override
    public String add() {
        Map<String, Long> nodesLoad = load();
        if (nodesLoad.isEmpty()) {
            try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(nodesLoadPath))) {
                List<String> nodesAddresses = DataSynchronizer.getInstance().getNodesAddresses();
                Map<String, Long> initLoad = nodesAddresses.stream().collect(Collectors.toMap(
                        str -> str,
                        str -> 0L
                ));
                oos.writeObject(initLoad);
                nodesLoad = initLoad;
            } catch (IOException e) {}
        }
        Optional<String> minKey = nodesLoad.keySet().stream().min(Comparator.comparingLong(nodesLoad::get));
        nodesLoad.put(minKey.get(), nodesLoad.get(minKey.get()) + 1);
        save(nodesLoad);
        return minKey.get();
    }

    @Override
    public void remove(List<Document> documents) {
        Map<String, Long> nodesLoad = load();
        documents.forEach(document -> {
            String documentAffinity = document.getValue("_affinity");
            nodesLoad.put(documentAffinity, nodesLoad.get(documentAffinity) - 1);
        });
        balance(nodesLoad);
        save(nodesLoad);
    }

    private void balance(Map<String, Long> map) {

    }
}
