package com.forerp.erp.user.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RoleCreateRequestDto {

    private String name;
    private String description;
}
