package kamel.capstone.nosqlnode.data.command;

import kamel.capstone.nosqlnode.data.DataAccessLayer;
import kamel.capstone.nosqlnode.data.model.UserRole;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Component
public class CommandFactory {
    private final DataAccessLayer dal;
    public static final String CREATE_COLLECTION = "CREATE_COLLECTION";
    public static final String DELETE_COLLECTION = "DELETE_COLLECTION";
    public static final String CREATE_INDEX = "CREATE_INDEX";
    public static final String DELETE_INDEX = "DELETE_INDEX";
    public static final String ATTRIBUTES = "ATTRIBUTES";
    public static final String INSERT_INTO = "INSERT_INTO";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE_FROM = "DELETE_FROM";
    public static final String WHERE = "WHERE";
    public static final String INTO = "INTO";
    public static final String DESCRIBE = "DESCRIBE";
    public static final String SELECT_FROM = "SELECT_FROM";

    public CommandFactory(@Qualifier("Simple") DataAccessLayer dal) {
        this.dal = dal;
    }

    public Command createCommand(String command, List<UserRole> roles) throws ClassNotFoundException, AccessDeniedException {
        if (command.startsWith(CREATE_COLLECTION)) {
            return new CreateCollectionCommand(command, dal, roles);
        } else if (command.startsWith(DELETE_COLLECTION)) {
            return new DeleteCollectionCommand(command, dal, roles);
        } else if (command.startsWith(INSERT_INTO)) {
            return new InsertDocumentCommand(command, dal, roles);
        } else if (command.startsWith(UPDATE)) {
            return new UpdateDocumentCommand(command, dal, roles);
        } else if (command.startsWith(DELETE_FROM)) {
            return new DeleteDocumentCommand(command, dal, roles);
        } else if (command.startsWith(SELECT_FROM)) {
            return new SelectDocumentCommand(command, dal, roles);
        } else if (command.startsWith(CREATE_INDEX)) {
            return new CreateIndexCommand(command, dal, roles);
        } else if (command.startsWith(DESCRIBE)) {
            return new DescribeCommand(command, dal, roles);
        } else {
            throw new ClassNotFoundException("Invalid command: " + command);
        }
    }
}
