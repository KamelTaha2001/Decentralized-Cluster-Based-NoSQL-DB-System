package kamel.capstone.nosqlnode.data.permission;

import kamel.capstone.nosqlnode.data.model.UserRole;

import java.util.List;

public class WritePermissionStrategy implements UserPermissionStrategy {
    @Override
    public boolean checkPermission(List<UserRole> roles) {
        return roles.stream().anyMatch(role -> role.name().equals(UserRole.ADMIN.name()));
    }
}
