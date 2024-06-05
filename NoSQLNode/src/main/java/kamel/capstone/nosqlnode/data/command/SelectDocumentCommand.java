package kamel.capstone.nosqlnode.data.command;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import kamel.capstone.nosqlnode.data.permission.ReadPermissionStrategy;
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

public class SelectDocumentCommand implements Command {
    private final String command;
    private final DataAccessLayer dal;
    private final UserPermissionStrategy permissionStrategy;

    public SelectDocumentCommand(String command, DataAccessLayer dal, List<UserRole> userRole) throws AccessDeniedException {
        this.command = command;
        this.dal = dal;
        this.permissionStrategy = new ReadPermissionStrategy();
        if (!permissionStrategy.checkPermission(userRole))
            throw new AccessDeniedException("Access denied: You role is not allowed to perform the selected operation!");
    }

    @Override
    public Result execute() throws NoSuchMethodException {
        Pattern pattern = Pattern.compile(
                CommandFactory.SELECT_FROM + "\\s*\\((.+?)\\)\\s+" +
                CommandFactory.ATTRIBUTES + "\\s*\\((.*?)\\)(?:\\s+" +
                CommandFactory.WHERE + "\\s*\\((.+?)\\))?"
        );
        Matcher matcher = pattern.matcher(command.trim());
        if (matcher.find()) {
            String collectionName = matcher.group(1);
            List<String> columns = new ArrayList<>(Arrays.stream(matcher.group(2).trim().split(",")).map(String::trim).toList());
            if (columns.size() == 1 && columns.get(0).isEmpty())
                columns.clear();
            try {
                if (matcher.group(3) != null) {
                    Map<String, String> conditions = CommandUtils.extractParametersMap(matcher.group(3));
                    String key = conditions.keySet().stream().findFirst().get();
                    return dal.select(collectionName, columns, List.of(new Condition(key, ConditionType.EQUAL, conditions.get(key))));
                } else {
                    return dal.select(collectionName, columns, Collections.emptyList());
                }
            } catch (ProcessingException e) {
                throw new NoSuchMethodException("Invalid command: cannot extract conditions.");
            }
        } else {
            throw new NoSuchMethodException("Invalid command: command is not complete.");
        }
    }

    @Override
    public CommandType getType() {
        return CommandType.READING_COMMAND;
    }
}
