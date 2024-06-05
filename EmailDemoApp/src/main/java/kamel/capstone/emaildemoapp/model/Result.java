package kamel.capstone.emaildemoapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class Result {
    private boolean success;
    private List<String> result;

    public Result() {}

    public Result(Boolean success, List<String> result) {
        this.success = success;
        this.result = result;
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
}
