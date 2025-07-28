package com.blockchain.blockpulseservice;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String txHash;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CryptoCurrency currency;
    
    @Column(nullable = false)
    private Long blockHeight;
    
    @Column(nullable = false)
    private Integer transactionSize; // in bytes
    
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal totalFee;
    
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal feePerByte;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(nullable = false)
    private LocalDateTime confirmedAt;
    
    @Builder.Default
    private Boolean isOutlier = false;
    
    @Builder.Default
    private Boolean isSpam = false;
    
    public enum CryptoCurrency {
        BTC, KASPA
    }
}