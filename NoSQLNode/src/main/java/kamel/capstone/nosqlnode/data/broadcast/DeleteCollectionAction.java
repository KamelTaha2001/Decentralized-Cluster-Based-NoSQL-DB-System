package kamel.capstone.nosqlnode.data.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;
import kamel.capstone.nosqlnode.data.model.Collection;
import kamel.capstone.nosqlnode.data.FileSystem;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

public class DeleteCollectionAction implements DataSyncingAction {
    @JsonProperty
    private Collection collection;
    public static final String TYPE = "delete_collection";

    public DeleteCollectionAction() {}

    public DeleteCollectionAction(Collection collection) {
        this.collection = collection;
    }

    @Override
    public void sync(FileSystem fileSystem) throws IOException {
        FileUtils.deleteDirectory(new File(collection.getPath()));
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }
}
