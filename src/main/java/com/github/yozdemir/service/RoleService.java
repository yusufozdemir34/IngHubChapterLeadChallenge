package com.github.yozdemir.service;

import com.github.yozdemir.domain.entity.Role;
import com.github.yozdemir.domain.enums.RoleType;
import com.github.yozdemir.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<Role> getReferenceByTypeIsIn(Set<RoleType> types) {
        return roleRepository.getReferenceByTypeIsIn(types);
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }
}
