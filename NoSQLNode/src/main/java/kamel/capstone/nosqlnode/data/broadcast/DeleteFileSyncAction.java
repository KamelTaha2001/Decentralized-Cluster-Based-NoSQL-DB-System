package kamel.capstone.nosqlnode.data.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;
import kamel.capstone.nosqlnode.data.FileSystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DeleteFileSyncAction implements DataSyncingAction {
    @JsonProperty
    List<String> paths;
    public static final String TYPE = "delete_file_sync";

    public DeleteFileSyncAction() {}

    public DeleteFileSyncAction(List<String> paths) {
        this.paths = paths;
    }

    @Override
    public void sync(FileSystem fileSystem) throws IOException {
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                if (!file.delete())
                    throw new IOException();
            }
        }
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }
}
