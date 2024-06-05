package kamel.capstone.nosqlnode.data.broadcast;

import com.fasterxml.jackson.annotation.JsonProperty;
import kamel.capstone.nosqlnode.data.FileSystem;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class WriteFileSyncAction implements DataSyncingAction {
    @JsonProperty
    Map<String, byte[]> files;
    public static final String TYPE = "write_file_sync";

    public WriteFileSyncAction() {}

    public WriteFileSyncAction(Map<String, byte[]> files) {
        this.files = files;
    }

    @Override
    public void sync(FileSystem fileSystem) throws IOException {
        for (String key : files.keySet()) {
            File file = new File(key);
            if (!file.exists())
                file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(files.get(key));
            fos.close();
        }
    }

    public Map<String, byte[]> getFiles() {
        return files;
    }

    public void setFiles(Map<String, byte[]> files) {
        this.files = files;
    }
}
