package com.github.yozdemir.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {

    PENDING("Pending"),
    APPROVED("Approved"),
    DENIED("Denied");

    private String label;
}
