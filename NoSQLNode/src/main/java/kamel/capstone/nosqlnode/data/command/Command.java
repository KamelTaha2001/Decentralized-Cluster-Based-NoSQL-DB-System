package kamel.capstone.nosqlnode.data.command;

import kamel.capstone.nosqlnode.data.model.Result;

public interface Command {
    Result execute() throws NoSuchMethodException;
    CommandType getType();
}
