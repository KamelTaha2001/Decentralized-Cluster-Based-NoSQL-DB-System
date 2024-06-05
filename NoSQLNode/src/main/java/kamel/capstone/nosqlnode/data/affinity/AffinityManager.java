package kamel.capstone.nosqlnode.data.affinity;

import kamel.capstone.nosqlnode.data.model.Document;
import org.springframework.stereotype.Component;

import javax.print.Doc;
import java.util.List;

@Component
public interface AffinityManager {
    String add();
    void remove(List<Document> document);
}
