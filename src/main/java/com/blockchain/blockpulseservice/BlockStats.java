package com.blockchain.blockpulseservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "block_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockStats {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long blockHeight;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Transaction.CryptoCurrency currency;
    
    @Column(nullable = false)
    private Integer transactionCount;
    
    @Column(nullable = false)
    private Long blockSize; // in bytes
    
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal avgFeePerByte;
    
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal medianFeePerByte;
    
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal minFeePerByte;
    
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal maxFeePerByte;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Builder.Default
    private Boolean isFull = false;
    
    @Builder.Default
    private Boolean hasFeeWar = false;
}