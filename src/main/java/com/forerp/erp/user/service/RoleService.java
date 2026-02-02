package com.forerp.erp.user.service;

import com.forerp.erp.user.domain.Role;
import com.forerp.erp.user.dto.RoleCreateRequestDto;
import com.forerp.erp.user.dto.RoleResponseDto;
import com.forerp.erp.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional
    public RoleResponseDto createRole(RoleCreateRequestDto request) {

        String name = request.getName();
        String description = request.getDescription();

        roleRepository.findByName(name)
                .ifPresent(role -> {
                    throw new IllegalArgumentException("이미 존재하는 역할 이름입니다." + name);
                });

        Role newRole = Role.builder()
                .name(name)
                .description(description)
                .build();

        Role savedRole = roleRepository.save(newRole);

        return new RoleResponseDto(savedRole);
    }
}

