package com.forerp.erp.user.service;

import com.forerp.erp.user.domain.Role;
import com.forerp.erp.user.domain.Permission;
import com.forerp.erp.user.repository.RoleRepository;
import com.forerp.erp.user.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class RolePermissionService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RolePermissionService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public void assignPermissionToRole(String roleName, String permissionCode) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Permission permission = permissionRepository.findByPermissionCode(permissionCode)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        if(role.getPermissions() == null) role.setPermissions(new HashSet<>());
        role.getPermissions().add(permission);
        roleRepository.save(role);
    }

    public Set<String> getPermissionsByRole(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Set<String> codes = new HashSet<>();
        if(role.getPermissions() != null) {
            role.getPermissions().forEach(p -> codes.add(p.getPermissionCode()));
        }
        return codes;
    }
}
