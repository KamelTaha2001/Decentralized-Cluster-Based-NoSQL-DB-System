package kamel.capstone.nosqlnode.data.broadcast;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import kamel.capstone.nosqlnode.data.FileSystem;

import java.io.IOException;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateCollectionAction.class, name = CreateCollectionAction.TYPE),
        @JsonSubTypes.Type(value = DeleteCollectionAction.class, name = DeleteCollectionAction.TYPE),
        @JsonSubTypes.Type(value = WriteFileSyncAction.class, name = WriteFileSyncAction.TYPE),
        @JsonSubTypes.Type(value = DeleteFileSyncAction.class, name = DeleteFileSyncAction.TYPE)
})
public interface DataSyncingAction {
    void sync(FileSystem fileSystem) throws IOException;
}
