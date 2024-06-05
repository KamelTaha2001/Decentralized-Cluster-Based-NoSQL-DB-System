package kamel.capstone.nosqlnode.data.model;

public class CommandDTO {
    private String command;

    public CommandDTO() {}

    public CommandDTO(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
