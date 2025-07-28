package com.blockchain.blockpulseservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByCurrencyAndTimestampBetween(
        Transaction.CryptoCurrency currency, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    List<Transaction> findByCurrencyAndBlockHeight(
        Transaction.CryptoCurrency currency, 
        Long blockHeight
    );
    
    @Query("SELECT t FROM Transaction t WHERE t.currency = :currency AND t.timestamp >= :since ORDER BY t.timestamp DESC")
    List<Transaction> findRecentTransactions(
        @Param("currency") Transaction.CryptoCurrency currency, 
        @Param("since") LocalDateTime since
    );
    
    @Query("SELECT t FROM Transaction t WHERE t.currency = :currency AND t.isOutlier = true AND t.timestamp >= :since")
    List<Transaction> findRecentOutliers(
        @Param("currency") Transaction.CryptoCurrency currency, 
        @Param("since") LocalDateTime since
    );
    
    @Query("SELECT t FROM Transaction t WHERE t.currency = :currency AND t.isSpam = true AND t.timestamp >= :since")
    List<Transaction> findRecentSpamTransactions(
        @Param("currency") Transaction.CryptoCurrency currency, 
        @Param("since") LocalDateTime since
    );
}