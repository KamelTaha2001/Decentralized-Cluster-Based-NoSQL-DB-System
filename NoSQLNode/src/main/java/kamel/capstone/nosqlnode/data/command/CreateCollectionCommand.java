package kamel.capstone.nosqlnode.data.command;

import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import kamel.capstone.nosqlnode.data.permission.WritePermissionStrategy;
import kamel.capstone.nosqlnode.data.permission.UserPermissionStrategy;
import kamel.capstone.nosqlnode.data.DataAccessLayer;
import kamel.capstone.nosqlnode.data.model.Result;
import kamel.capstone.nosqlnode.data.model.UserRole;
import kamel.capstone.nosqlnode.util.CommandUtils;
import kamel.capstone.nosqlnode.util.JsonUtils;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateCollectionCommand implements Command {
    private final String command;
    private final DataAccessLayer dal;
    private final UserPermissionStrategy permissionStrategy;

    public CreateCollectionCommand(String command, DataAccessLayer dal, List<UserRole> userRole) throws AccessDeniedException {
        this.command = command;
        this.dal = dal;
        this.permissionStrategy = new WritePermissionStrategy();
        if (!permissionStrategy.checkPermission(userRole))
            throw new AccessDeniedException("Access denied: You role is not allowed to perform the selected operation!");
    }

    @Override
    public Result execute() throws NoSuchMethodException {
        Pattern pattern = Pattern.compile(
                CommandFactory.CREATE_COLLECTION + "\\s*\\((.+?)\\)\\s+"
                        + CommandFactory.ATTRIBUTES + "\\s*\\((.+?)\\)"
        );
        Matcher matcher = pattern.matcher(command.trim());
        if (matcher.find()) {
            String collectionName = matcher.group(1);
            try {
                Map<String, String> attributes = CommandUtils.extractAttributesMap(matcher.group(2));
                attributes.put("_id", "integer");
                attributes.put("_affinity", "string");
                String schema = JsonUtils.generateJsonSchema(attributes);
                return dal.createCollection(collectionName, schema);
            } catch (ProcessingException e) {
                throw new NoSuchMethodException("Invalid command: cannot extract attributes.");
            }
        } else {
            throw new NoSuchMethodException("Invalid command: command is not complete.");
        }
    }

    @Override
    public CommandType getType() {
        return CommandType.CREATIONAL_COMMAND;
    }
}
