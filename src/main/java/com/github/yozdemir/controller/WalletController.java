package com.github.yozdemir.controller;

import com.github.yozdemir.dto.request.TransactionRequest;
import com.github.yozdemir.dto.request.WalletRequest;
import com.github.yozdemir.dto.response.CommandResponse;
import com.github.yozdemir.dto.response.WalletResponse;
import com.github.yozdemir.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PreAuthorize("hasRole(T(com.github.yozdemir.domain.enums.RoleType).ROLE_USER)")
    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> findById(@PathVariable long id) {
        final WalletResponse response = walletService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole(T(com.github.yozdemir.domain.enums.RoleType).ROLE_USER)")
    @GetMapping("/iban/{iban}")
    public ResponseEntity<WalletResponse> findByIban(@PathVariable String iban) {
        final WalletResponse response = walletService.findByIban(iban);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole(T(com.github.yozdemir.domain.enums.RoleType).ROLE_USER)")
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<WalletResponse>> findByUserId(@PathVariable long userId) {
        final List<WalletResponse> response = walletService.findByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole(T(com.github.yozdemir.domain.enums.RoleType).ROLE_USER)")
    @GetMapping
    public ResponseEntity<Page<WalletResponse>> findAll(Pageable pageable) {
        final Page<WalletResponse> response = walletService.findAll(pageable);
        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole(T(com.github.yozdemir.domain.enums.RoleType).ROLE_USER)")
    @PostMapping
    public ResponseEntity<CommandResponse> create(@Valid @RequestBody WalletRequest request) {
        final CommandResponse response = walletService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole(T(com.github.yozdemir.domain.enums.RoleType).ROLE_USER)")
    @PostMapping("/addFunds")
    public ResponseEntity<CommandResponse> addFunds(@Valid @RequestBody TransactionRequest request) {
        final CommandResponse response = walletService.addFunds(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PreAuthorize("hasRole(T(com.github.yozdemir.domain.enums.RoleType).ROLE_USER)")
    @PutMapping("/{id}")
    public ResponseEntity<CommandResponse> update(@PathVariable long id, @Valid @RequestBody WalletRequest request) {
        final CommandResponse response = walletService.update(id, request);
        return ResponseEntity.ok(response);
    }

}
