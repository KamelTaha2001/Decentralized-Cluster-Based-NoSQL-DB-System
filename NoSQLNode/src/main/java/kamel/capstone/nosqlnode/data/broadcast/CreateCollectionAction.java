package kamel.capstone.nosqlnode.data.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;
import kamel.capstone.nosqlnode.data.model.Collection;
import kamel.capstone.nosqlnode.data.FileSystem;
import org.springframework.stereotype.Component;

public class CreateCollectionAction implements DataSyncingAction {
    @JsonProperty
    private Collection collection;
    public static final String TYPE = "create_collection";

    public CreateCollectionAction() {}

    public CreateCollectionAction(Collection collection) {
        this.collection = collection;
    }

    @Override
    public void sync(FileSystem fileSystem) {
        fileSystem.saveCollection(collection);
    }

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }
}
