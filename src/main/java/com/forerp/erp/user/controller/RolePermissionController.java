package com.forerp.erp.user.controller;

import com.forerp.erp.user.service.RolePermissionService;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/roles")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    public RolePermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @PostMapping("/{roleName}/permissions/{permissionCode}")
    public void assignPermission(@PathVariable String roleName,
                                 @PathVariable String permissionCode) {
        rolePermissionService.assignPermissionToRole(roleName, permissionCode);
    }

    @GetMapping("/{roleName}/permissions")
    public Set<String> getPermissions(@PathVariable String roleName) {
        return rolePermissionService.getPermissionsByRole(roleName);
    }
}
