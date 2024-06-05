package kamel.capstone.nosqlnode.data.command;

import kamel.capstone.nosqlnode.data.permission.WritePermissionStrategy;
import kamel.capstone.nosqlnode.data.permission.UserPermissionStrategy;
import kamel.capstone.nosqlnode.data.DataAccessLayer;
import kamel.capstone.nosqlnode.data.model.Result;
import kamel.capstone.nosqlnode.data.model.UserRole;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateIndexCommand implements Command {
    private final String command;
    private final DataAccessLayer dal;
    private final UserPermissionStrategy permissionStrategy;

    public CreateIndexCommand(String command, DataAccessLayer dal, List<UserRole> userRole) throws AccessDeniedException {
        this.command = command;
        this.dal = dal;
        this.permissionStrategy = new WritePermissionStrategy();
        if (!permissionStrategy.checkPermission(userRole))
            throw new AccessDeniedException("Access denied: You role is not allowed to perform the selected operation!");
    }

    @Override
    public Result execute() throws NoSuchMethodException {
        Pattern pattern = Pattern.compile(
                CommandFactory.CREATE_INDEX + "\\s*\\((.+?)\\)\\s+" +
                        CommandFactory.INTO + "\\s*\\((.*?)\\)"
        );
        Matcher matcher = pattern.matcher(command.trim());
        if (matcher.find()) {
            String column = matcher.group(1);
            String collectionName = matcher.group(2);
            return dal.createIndex(collectionName, column);
        } else {
            throw new NoSuchMethodException("Invalid command: cannot extract db name.");
        }
    }

    @Override
    public CommandType getType() {
        return CommandType.CREATIONAL_COMMAND;
    }
}
