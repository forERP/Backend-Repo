package com.forerp.erp.user.dto;

import com.forerp.erp.user.domain.Permission;
import com.forerp.erp.user.domain.Role;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class RoleResponseDto {

    private final Long id;
    private final String name;
    private final String description;

    private final Set<String> permissions;

    public RoleResponseDto(Role role){
        this.id = role.getId();
        this.name = role.getName();
        this.description = role.getDescription();
        this.permissions = role.getPermissions().stream()
                .map(Permission::name)
                .collect(Collectors.toSet());
    }
}
