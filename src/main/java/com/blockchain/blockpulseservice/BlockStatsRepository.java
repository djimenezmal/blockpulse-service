package com.blockchain.blockpulseservice;

import com.crypto.feemarketcomparator.model.BlockStats;
import com.crypto.feemarketcomparator.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlockStatsRepository extends JpaRepository<BlockStats, Long> {
    
    Optional<BlockStats> findByCurrencyAndBlockHeight(
        Transaction.CryptoCurrency currency, 
        Long blockHeight
    );
    
    List<BlockStats> findByCurrencyAndTimestampBetween(
        Transaction.CryptoCurrency currency, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    @Query("SELECT bs FROM BlockStats bs WHERE bs.currency = :currency AND bs.timestamp >= :since ORDER BY bs.timestamp DESC")
    List<BlockStats> findRecentBlockStats(
        @Param("currency") Transaction.CryptoCurrency currency, 
        @Param("since") LocalDateTime since
    );
    
    @Query("SELECT bs FROM BlockStats bs WHERE bs.currency = :currency AND bs.hasFeeWar = true AND bs.timestamp >= :since")
    List<BlockStats> findRecentFeeWars(
        @Param("currency") Transaction.CryptoCurrency currency, 
        @Param("since") LocalDateTime since
    );
}