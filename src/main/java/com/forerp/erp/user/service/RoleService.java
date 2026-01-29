package com.forerp.erp.user.service;

import com.forerp.erp.user.domain.Role;
import com.forerp.erp.user.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role createRole(String name, String description) {
        roleRepository.findByName(name)
                .ifPresent(r -> { throw new IllegalStateException("Role exists"); });

        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        return roleRepository.save(role);
    }
}

