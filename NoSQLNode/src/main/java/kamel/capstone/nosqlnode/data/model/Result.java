package kamel.capstone.nosqlnode.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.print.Doc;
import java.util.List;

public class Result {
    private boolean success;
    private List<String> result;
    @JsonIgnore
    private List<Document> documents;

    public Result(Boolean success, List<Document> documents) {
        this.success = success;
        this.documents = documents;
        this.result = documents == null ? null : documents.stream().map(Document::toString).toList();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getResult() {
        return result;
    }

    public void setResult(List<String> result) {
        this.result = result;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
}
