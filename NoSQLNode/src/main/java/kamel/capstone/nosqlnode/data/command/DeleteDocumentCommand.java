package kamel.capstone.nosqlnode.data.command;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import kamel.capstone.nosqlnode.data.permission.WritePermissionStrategy;
import kamel.capstone.nosqlnode.data.permission.UserPermissionStrategy;
import kamel.capstone.nosqlnode.data.model.Condition;
import kamel.capstone.nosqlnode.data.model.ConditionType;
import kamel.capstone.nosqlnode.data.DataAccessLayer;
import kamel.capstone.nosqlnode.data.model.Result;
import kamel.capstone.nosqlnode.data.model.UserRole;
import kamel.capstone.nosqlnode.util.CommandUtils;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeleteDocumentCommand implements Command {
    private final String command;
    private final DataAccessLayer dal;
    private final UserPermissionStrategy permissionStrategy;

    public DeleteDocumentCommand(String command, DataAccessLayer dal, List<UserRole> userRole) throws AccessDeniedException {
        this.command = command;
        this.dal = dal;
        this.permissionStrategy = new WritePermissionStrategy();
        if (!permissionStrategy.checkPermission(userRole))
            throw new AccessDeniedException("Access denied: You role is not allowed to perform the selected operation!");
    }

    @Override
    public Result execute() throws NoSuchMethodException {
        Pattern pattern = Pattern.compile(
                CommandFactory.DELETE_FROM + "\\s*\\((.+?)\\)(?:\\s+"
                        + CommandFactory.WHERE + "\\s*\\((.+?)\\))?"
        );
        Matcher matcher = pattern.matcher(command.trim());
        if (matcher.find()) {
            String collectionName = matcher.group(1);
            try {
                if (matcher.group(2) != null) {
                    Map<String, String> conditions = CommandUtils.extractParametersMap(matcher.group(2));
                    String key = conditions.keySet().stream().findFirst().get();
                    return dal.delete(collectionName, List.of(new Condition(key, ConditionType.EQUAL, conditions.get(key))));
                } else {
                    return dal.delete(collectionName, Collections.emptyList());
                }
            } catch (ProcessingException e) {
                throw new NoSuchMethodException("Invalid command: cannot extract attributes.");
            }
        } else {
            throw new NoSuchMethodException("Invalid command: command is not complete.");
        }
    }

    @Override
    public CommandType getType() {
        return CommandType.MODIFYING_COMMAND;
    }
}
