package com.github.yozdemir.controller;

import com.github.yozdemir.dto.response.TransactionResponse;
import com.github.yozdemir.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PreAuthorize("hasRole(T(com.github.yozdemir.domain.enums.RoleType).ROLE_USER)")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(@PathVariable long id) {
        final TransactionResponse response = transactionService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole(T(com.github.yozdemir.domain.enums.RoleType).ROLE_USER)")
    @PostMapping("/pending/{id}")
    public ResponseEntity<TransactionResponse> approve(@PathVariable long id) {
        final TransactionResponse response = transactionService.approveOrDenyTransaction(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole(T(com.github.yozdemir.domain.enums.RoleType).ROLE_USER)")
    @GetMapping("/references/{referenceNumber}")
    public ResponseEntity<TransactionResponse> findByReferenceNumber(@PathVariable UUID referenceNumber) {
        final TransactionResponse response = transactionService.findByReferenceNumber(referenceNumber);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole(T(com.github.yozdemir.domain.enums.RoleType).ROLE_USER)")
    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<TransactionResponse>> findAllByUserId(@PathVariable long userId) {
        final Page<TransactionResponse> response = new PageImpl<>(transactionService.findAllByUserId(userId));
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole(T(com.github.yozdemir.domain.enums.RoleType).ROLE_USER)")
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> findAll(Pageable pageable) {
        final Page<TransactionResponse> response = transactionService.findAll(pageable);
        return ResponseEntity.ok(response);
    }
}
