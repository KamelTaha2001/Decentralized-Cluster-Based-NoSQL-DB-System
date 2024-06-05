package kamel.capstone.nosqlnode.data.command;

import kamel.capstone.nosqlnode.data.DataAccessLayer;
import kamel.capstone.nosqlnode.data.permission.UserPermissionStrategy;
import kamel.capstone.nosqlnode.data.permission.WritePermissionStrategy;
import kamel.capstone.nosqlnode.data.model.Result;
import kamel.capstone.nosqlnode.data.model.UserRole;

import java.nio.file.AccessDeniedException;
import java.util.*;

public class DescribeCommand implements Command {
    private final String command;
    private final DataAccessLayer dal;
    private final UserPermissionStrategy permissionStrategy;

    public DescribeCommand(String command, DataAccessLayer dal, List<UserRole> userRole) throws AccessDeniedException {
        this.command = command;
        this.dal = dal;
        this.permissionStrategy = new WritePermissionStrategy();
        if (!permissionStrategy.checkPermission(userRole))
            throw new AccessDeniedException("Access denied: You role is not allowed to perform the selected operation!");
    }

    @Override
    public Result execute() throws NoSuchMethodException {
        if (command.equals("DESCRIBE()")) {
            return dal.describe();
        } else {
            throw new NoSuchMethodException("Invalid command: command is not complete.");
        }
    }

    @Override
    public CommandType getType() {
        return CommandType.READING_COMMAND;
    }
}
