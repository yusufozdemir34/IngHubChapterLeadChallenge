package com.github.yozdemir.dto.response;

import com.github.yozdemir.domain.enums.RoleType;
import lombok.Data;

/**
 * Data Transfer Object for Role response.
 */
@Data
public class RoleResponse {

    private Long id;
    private RoleType type;
}
