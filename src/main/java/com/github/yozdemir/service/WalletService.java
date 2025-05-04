package com.github.yozdemir.service;

import com.github.yozdemir.config.MessageSourceConfig;
import com.github.yozdemir.domain.entity.Wallet;
import com.github.yozdemir.domain.enums.Status;
import com.github.yozdemir.dto.mapper.WalletRequestMapper;
import com.github.yozdemir.dto.mapper.WalletResponseMapper;
import com.github.yozdemir.dto.mapper.WalletTransactionRequestMapper;
import com.github.yozdemir.dto.request.TransactionRequest;
import com.github.yozdemir.dto.request.WalletRequest;
import com.github.yozdemir.dto.response.CommandResponse;
import com.github.yozdemir.dto.response.WalletResponse;
import com.github.yozdemir.exception.ElementAlreadyExistsException;
import com.github.yozdemir.exception.InsufficientFundsException;
import com.github.yozdemir.exception.NoSuchElementFoundException;
import com.github.yozdemir.repository.WalletRepository;
import com.github.yozdemir.validator.IbanValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static com.github.yozdemir.common.MessageKeys.*;

/**
 * Service used for Wallet related operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final MessageSourceConfig messageConfig;
    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final WalletRequestMapper walletRequestMapper;
    private final WalletResponseMapper walletResponseMapper;
    private final WalletTransactionRequestMapper walletTransactionRequestMapper;
    private final IbanValidator ibanValidator;

    private final BigDecimal moneyApprovalLimit = new BigDecimal("1000.00");

    /**
     * Fetches a single wallet by the given id.
     *
     * @param id
     * @return WalletResponse
     */
    @Transactional(readOnly = true)
    public WalletResponse findById(long id) {
        return walletRepository.findById(id)
                .map(walletResponseMapper::toWalletResponse)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_WALLET_NOT_FOUND)));
    }

    /**
     * Fetches a single wallet by the given iban.
     *
     * @param iban
     * @return WalletResponse
     */
    @Transactional(readOnly = true)
    public WalletResponse findByIban(String iban) {
        return walletRepository.findByIban(iban)
                .map(walletResponseMapper::toWalletResponse)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_WALLET_NOT_FOUND)));
    }

    /**
     * Fetches a single wallet by the given userId.
     *
     * @param userId
     * @return WalletResponse
     */
    @Transactional(readOnly = true)
    public List<WalletResponse> findByUserId(long userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(walletResponseMapper::toWalletResponse)
                .toList();
    }

    /**
     * Fetches a single wallet reference (entity) by the given id.
     *
     * @param iban
     * @return Wallet
     */
    public Wallet getByIban(String iban) {
        return walletRepository.findByIban(iban)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_WALLET_NOT_FOUND)));
    }

    /**
     * Fetches all wallets based on the given paging and sorting parameters.
     *
     * @param pageable
     * @return List of WalletResponse
     */
    @Transactional(readOnly = true)
    public Page<WalletResponse> findAll(Pageable pageable) {
        final Page<Wallet> wallets = walletRepository.findAll(pageable);
        if (wallets.isEmpty())
            throw new NoSuchElementFoundException(messageConfig.getMessage(ERROR_NO_RECORDS));
        return wallets.map(walletResponseMapper::toWalletResponse);
    }

    /**
     * Creates a new wallet using the given request parameters.
     *
     * @param request
     * @return id of the created wallet
     */
    @Transactional
    public CommandResponse create(WalletRequest request) {
        if (walletRepository.existsByIbanIgnoreCase(request.getIban()))
            throw new ElementAlreadyExistsException(messageConfig.getMessage(ERROR_WALLET_IBAN_EXISTS));
        if (walletRepository.existsByUserIdAndNameIgnoreCase(request.getUserId(), request.getName()))
            throw new ElementAlreadyExistsException(messageConfig.getMessage(ERROR_WALLET_NAME_EXISTS));

        ibanValidator.isValid(request.getIban(), null);

        final Wallet wallet = walletRequestMapper.toWallet(request);
        walletRepository.save(wallet);
        log.info(messageConfig.getMessage(INFO_WALLET_CREATED, wallet.getIban(), wallet.getName(), wallet.getBalance()));

        // add this initial amount to the transactions
        transactionService.create(walletTransactionRequestMapper.toTransactionRequest(request));

        return CommandResponse.builder().id(wallet.getId()).build();
    }

    /**
     * Transfer funds between wallets.
     *
     * @param request
     * @return id of the transaction
     */
    @Transactional
    public CommandResponse transferFunds(TransactionRequest request) {
        final Wallet toWallet = getByIban(request.getToWalletIban());
        final Wallet fromWallet = getByIban(request.getFromWalletIban());

        // check if the balance of sender wallet has equal or higher to/than transfer amount
        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0)
            throw new InsufficientFundsException(messageConfig.getMessage(ERROR_INSUFFICIENT_FUNDS));

        // update balance of the sender wallet
        fromWallet.setBalance(fromWallet.getBalance().subtract(request.getAmount()));

        // update balance of the receiver wallet
        toWallet.setBalance(toWallet.getBalance().add(request.getAmount()));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);
        log.info(messageConfig.getMessage(INFO_WALLET_BALANCES_UPDATED, fromWallet.getBalance(), toWallet.getBalance()));

        final CommandResponse response = transactionService.create(request);
        return CommandResponse.builder().id(response.id()).build();
    }

    /**
     * Adds funds to the given wallet.
     *
     * @param request
     * @return id of the transaction
     */
    @Transactional
    public CommandResponse addFunds(TransactionRequest request) {
        final Wallet toWallet = getByIban(request.getToWalletIban());

        addAccordingToLimits(toWallet,request);

        walletRepository.save(toWallet);
        log.info(messageConfig.getMessage(INFO_WALLET_BALANCE_UPDATED, toWallet.getBalance()));

        final CommandResponse response = transactionService.create(request);
        return CommandResponse.builder().id(response.id()).build();
    }

    void addAccordingToLimits(final Wallet toWallet,TransactionRequest request){
        final BigDecimal amount = request.getAmount();
        // update balance of the receiver wallet
        toWallet.setUsableBalance(toWallet.getUsableBalance().add(amount));
        if (amount.compareTo(moneyApprovalLimit)<0) {
            toWallet.setBalance(toWallet.getBalance().add(amount));
            request.setStatus(Status.APPROVED);
        }
        else request.setStatus(Status.PENDING);
    }

    /**
     * Withdraw funds from the given wallet.
     *
     * @param request
     * @return id of the transaction
     */
    @Transactional
    public CommandResponse withdrawFunds(TransactionRequest request) {
        final Wallet fromWallet = getByIban(request.getFromWalletIban());

        // check if the balance of sender wallet has equal or higher to/than transfer amount
        if (fromWallet.getBalance().compareTo(request.getAmount()) < 0)
            throw new InsufficientFundsException(messageConfig.getMessage(ERROR_INSUFFICIENT_FUNDS));

        final BigDecimal amount = request.getAmount();
        // update balance of the sender wallet
        fromWallet.setUsableBalance(fromWallet.getUsableBalance().subtract(amount));
        if (amount.compareTo(moneyApprovalLimit)<0) {
            fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
            request.setStatus(Status.APPROVED);
        }
        else request.setStatus(Status.PENDING);

        walletRepository.save(fromWallet);
        log.info(messageConfig.getMessage(INFO_WALLET_BALANCE_UPDATED, fromWallet.getBalance()));

        final CommandResponse response = transactionService.create(request);
        return CommandResponse.builder().id(response.id()).build();
    }

    /**
     * Updates wallet using the given request parameters.
     *
     * @param request
     * @return id of the updated wallet
     */
    public CommandResponse update(long id, WalletRequest request) {
        final Wallet foundWallet = walletRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_WALLET_NOT_FOUND)));

        // check if the iban is changed and new iban is already exists
        if (!request.getIban().equalsIgnoreCase(foundWallet.getIban()) &&
                walletRepository.existsByIbanIgnoreCase(request.getIban()))
            throw new ElementAlreadyExistsException(messageConfig.getMessage(ERROR_WALLET_IBAN_EXISTS));

        // check if the name is changed and new name is already exists in user's wallets
        if (!request.getName().equalsIgnoreCase(foundWallet.getName()) &&
                walletRepository.existsByUserIdAndNameIgnoreCase(request.getUserId(), request.getName()))
            throw new ElementAlreadyExistsException(messageConfig.getMessage(ERROR_WALLET_NAME_EXISTS));

        ibanValidator.isValid(request.getIban(), null);

        final Wallet wallet = walletRequestMapper.toWallet(request);
        walletRepository.save(wallet);
        log.info(messageConfig.getMessage(INFO_WALLET_UPDATED, wallet.getIban(), wallet.getName(), wallet.getBalance()));
        return CommandResponse.builder().id(id).build();
    }

    /**
     * Deletes wallet by the given id.
     *
     * @param id
     */
    public void deleteById(long id) {
        final Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementFoundException(messageConfig.getMessage(ERROR_WALLET_NOT_FOUND)));
        walletRepository.delete(wallet);
        log.info(messageConfig.getMessage(INFO_WALLET_DELETED, wallet.getIban(), wallet.getName(), wallet.getBalance()));
    }
}
