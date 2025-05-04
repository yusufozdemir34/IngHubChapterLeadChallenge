package com.github.yozdemir.service;

import com.github.yozdemir.config.MessageSourceConfig;
import com.github.yozdemir.domain.entity.Transaction;
import com.github.yozdemir.domain.entity.Wallet;
import com.github.yozdemir.domain.enums.Status;
import com.github.yozdemir.dto.mapper.TransactionRequestMapper;
import com.github.yozdemir.dto.mapper.TransactionResponseMapper;
import com.github.yozdemir.dto.mapper.TransactionResponseMapperImpl;
import com.github.yozdemir.dto.mapper.WalletResponseMapper;
import com.github.yozdemir.dto.request.TransactionRequest;
import com.github.yozdemir.dto.response.CommandResponse;
import com.github.yozdemir.dto.response.TransactionResponse;
import com.github.yozdemir.exception.NoSuchElementFoundException;
import com.github.yozdemir.repository.TransactionRepository;
import com.github.yozdemir.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.github.yozdemir.common.MessageKeys.*;

/**
 * Service used for Transaction related operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final MessageSourceConfig messageConfig;
    private final TransactionRepository transactionRepository;

    private final WalletRepository walletRepository;
    private final TransactionRequestMapper transactionRequestMapper;
    private final TransactionResponseMapper transactionResponseMapper;

    private final WalletResponseMapper walletResponseMapper;

    @Transactional
    public TransactionResponse approveOrDenyTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        if (!transaction.getStatus().equals(Status.PENDING)) {
            throw new IllegalStateException("Only pending transactions can be updated.");
        }

        transaction.setStatus(Status.APPROVED);

        Wallet toWallet = transaction.getToWallet();
        BigDecimal amount = transaction.getAmount();

        if (transaction.getType().getId() == 1) {
            toWallet.setBalance(toWallet.getBalance().add(amount));
        } else {
            toWallet.setBalance(toWallet.getBalance().subtract(amount));
        }

        walletRepository.save(toWallet);

        Transaction updatedTransaction = transactionRepository.save(transaction);

        return mapToResponse(updatedTransaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        // MapStruct veya manuel dönüşüm burada yapılabilir.
        // Örneğin:
        return new TransactionResponseMapperImpl().toTransactionResponse(transaction);
    }

    /**
     * Fetches a single transaction by the given id.
     *
     * @param id
     * @return TransactionResponse
     */
    @Transactional(readOnly = true)
    public TransactionResponse findById(long id) {
        return transactionRepository.findById(id)
                .map(transactionResponseMapper::toTransactionResponse)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_TRANSACTION_NOT_FOUND)));
    }
  /*  @Transactional(readOnly = true)
    public TransactionResponse statusUpdate(long id) {
         transactionRepository.findById(id).;
         return null;
    }*/

    /**
     * Fetches a single transaction by the given referenceNumber.
     *
     * @param referenceNumber
     * @return TransactionResponse
     */
    @Transactional(readOnly = true)
    public TransactionResponse findByReferenceNumber(UUID referenceNumber) {
        return transactionRepository.findByReferenceNumber(referenceNumber)
                .map(transactionResponseMapper::toTransactionResponse)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_TRANSACTION_NOT_FOUND)));
    }

    /**
     * Fetches all transaction by the given userId.
     *
     * @param userId
     * @return List of TransactionResponse
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> findAllByUserId(Long userId) {
        final List<Transaction> transactions = transactionRepository.findAllByUserId(userId);
        if (transactions.isEmpty())
            throw new NoSuchElementFoundException(messageConfig.getMessage(ERROR_NO_RECORDS));

        return transactions.stream().map(transactionResponseMapper::toTransactionResponse)
                .toList();
    }

    /**
     * Fetches all transactions based on the given paging and sorting parameters.
     *
     * @param pageable
     * @return List of TransactionResponse
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> findAll(Pageable pageable) {
        final Page<Transaction> transactions = transactionRepository.findAll(pageable);
        if (transactions.isEmpty())
            throw new NoSuchElementFoundException(messageConfig.getMessage(ERROR_NO_RECORDS));

        return transactions.map(transactionResponseMapper::toTransactionResponse);
    }

    /**
     * Creates a new transaction using the given request parameters.
     *
     * @param request
     * @return id of the created transaction
     */
    public CommandResponse create(TransactionRequest request) {
        final Transaction transaction = transactionRequestMapper.toTransaction(request);
        transactionRepository.save(transaction);
        log.info(messageConfig.getMessage(INFO_TRANSACTION_CREATED, transaction.getFromWallet().getIban(), transaction.getToWallet().getIban(), transaction.getAmount()));
        return CommandResponse.builder().id(transaction.getId()).build();
    }
}
