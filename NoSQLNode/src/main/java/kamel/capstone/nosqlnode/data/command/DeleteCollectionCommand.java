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

public class DeleteCollectionCommand implements Command {
    private final String command;
    private DataAccessLayer dal;
    private final UserPermissionStrategy permissionStrategy;

    public DeleteCollectionCommand(String command, DataAccessLayer dal, List<UserRole> userRole) throws AccessDeniedException {
        this.command = command;
        this.dal = dal;
        this.permissionStrategy = new WritePermissionStrategy();
        if (!permissionStrategy.checkPermission(userRole))
            throw new AccessDeniedException("Access denied: You role is not allowed to perform the selected operation!");
    }

    @Override
    public Result execute() throws NoSuchMethodException {
        Pattern pattern = Pattern.compile(CommandFactory.DELETE_COLLECTION + "\\s*\\((.*?)\\)");
        Matcher matcher = pattern.matcher(command.trim());
        if (matcher.find()) {
            String collectionName = matcher.group(1);
            return dal.deleteCollection(collectionName);
        } else {
            throw new NoSuchMethodException("Invalid command: cannot extract collection name.");
        }
    }

    @Override
    public CommandType getType() {
        return CommandType.MODIFYING_COMMAND;
    }
}
