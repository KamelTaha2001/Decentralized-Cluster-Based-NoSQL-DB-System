package kamel.capstone.nosqlnode.controller;

import kamel.capstone.nosqlnode.data.FileSystem;
import kamel.capstone.nosqlnode.data.broadcast.DataSynchronizer;
import kamel.capstone.nosqlnode.data.broadcast.DataSyncingAction;
import kamel.capstone.nosqlnode.data.broadcast.WriteFileSyncAction;
import kamel.capstone.nosqlnode.data.model.*;
import kamel.capstone.nosqlnode.service.NoSQLNodeService;
import kamel.capstone.nosqlnode.util.Constants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

@RestController
@RequestMapping("/")
public class NoSQLNodeController {
    private final NoSQLNodeService service;
    private final FileSystem fileSystem;

    public NoSQLNodeController(
            @Qualifier("NoSQLServiceImpl") NoSQLNodeService service,
            FileSystem fileSystem
    ) {
        this.service = service;
        this.fileSystem = fileSystem;
    }

    @PostMapping("/bootstrap/nodes-addresses")
    public void setAddresses(@RequestBody Collection<Node> nodes) {
        service.setOtherNodes(nodes.stream().toList());
    }

    @PostMapping("/bootstrap/node-users")
    public void setUsers(@RequestBody Collection<User> users) {
        service.setUsers(new LinkedList<>(users.stream().toList()));
    }

    @PostMapping("/bootstrap/add-user")
    public void addUser(@RequestBody User user) {
        service.addUser(user);
    }

    @GetMapping("/bootstrap/stats")
    public ResponseEntity<Stats> getStats() {
        return ResponseEntity.ok(service.getStats());
    }

    @PostMapping("/execute")
    public ResponseEntity<Result> executeCommand(@RequestBody CommandDTO command, @RequestHeader("Authorization") String token) {
        try {
            User user = service.authenticate(token);
            return ResponseEntity.ok(service.executeCommand(command.getCommand(), user.getRoles()));
        } catch (AccessDeniedException e) {
            if (token.equals(Constants.PRIVATE_KEY)) {
                return ResponseEntity.ok(service.executeBroadcastCommand(command.getCommand()));
            } else
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Result(false, Collections.emptyList()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserDTO userDTO) {
        try {
            String token = service.authenticate(userDTO);
            return ResponseEntity.ok(token);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
        }
    }

    @PostMapping("/nosql/data-broadcast")
    public ResponseEntity<String> broadcast(@RequestBody DataSyncingAction syncingAction) throws IOException {
        System.out.println("EXECUTING BROADCAST: " + syncingAction.getClass());
        syncingAction.sync(fileSystem);
        return ResponseEntity.ok("");
    }
}
