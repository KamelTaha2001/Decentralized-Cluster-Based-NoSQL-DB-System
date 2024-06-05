package kamel.capstone.nosqlnode.data.permission;

import kamel.capstone.nosqlnode.data.model.UserRole;

import java.util.List;

public interface UserPermissionStrategy {
    boolean checkPermission(List<UserRole> role);
}
