package com.github.yozdemir.domain.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@EqualsAndHashCode(of = {"iban"})
@Table(indexes = {
        @Index(name = "wallet_user_id_iban_key", columnList = "user_id, iban", unique = true),
        @Index(name = "wallet_user_id_name_key", columnList = "user_id, name", unique = true)
})
public class Wallet {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "wallet_seq_gen"
    )
    @SequenceGenerator(
            name = "wallet_seq_gen",
            sequenceName = "wallet_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(length = 34, nullable = false, unique = true)
    private String iban;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(nullable = false)
    private Boolean activeForShopping = true;

    @Column(nullable = false)
    private Boolean activeForWithdraw = true;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private BigDecimal usableBalance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private Users user;

    @OneToMany(mappedBy = "fromWallet", cascade = CascadeType.ALL)
    private Set<Transaction> fromTransactions = new HashSet<>();

    public void addFromTransaction(Transaction transaction) {
        fromTransactions.add(transaction);
        transaction.setFromWallet(this);
    }

    public void removeFromTransaction(Transaction transaction) {
        fromTransactions.remove(transaction);
        transaction.setFromWallet(null);
    }

    @OneToMany(mappedBy = "toWallet", cascade = CascadeType.ALL)
    private Set<Transaction> toTransactions = new HashSet<>();

    public void addToTransaction(Transaction transaction) {
        toTransactions.add(transaction);
        transaction.setToWallet(this);
    }

    public void removeToTransaction(Transaction transaction) {
        toTransactions.remove(transaction);
        transaction.setToWallet(null);
    }
}


















