package com.blockchain.blockpulseservice;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FeeAnalysisController {
    
    private final com.crypto.feemarketcomparator.service.TransactionService transactionService;
    private final TransactionRepository transactionRepository;
    private final BlockStatsRepository blockStatsRepository;
    
    @GetMapping("/analysis/{currency}")
    public ResponseEntity<FeeAnalysisDTO> getFeeAnalysis(@PathVariable String currency) {
        try {
            Transaction.CryptoCurrency cryptoCurrency = Transaction.CryptoCurrency.valueOf(currency.toUpperCase());
            FeeAnalysisDTO analysis = transactionService.getCurrentFeeAnalysis(cryptoCurrency);
            return ResponseEntity.ok(analysis);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/transactions/{currency}")
    public ResponseEntity<List<Transaction>> getRecentTransactions(
            @PathVariable String currency,
            @RequestParam(defaultValue = "1") int hours) {
        try {
            Transaction.CryptoCurrency cryptoCurrency = Transaction.CryptoCurrency.valueOf(currency.toUpperCase());
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            List<Transaction> transactions = transactionRepository.findRecentTransactions(cryptoCurrency, since);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/transactions/{currency}/outliers")
    public ResponseEntity<List<Transaction>> getOutlierTransactions(
            @PathVariable String currency,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            Transaction.CryptoCurrency cryptoCurrency = Transaction.CryptoCurrency.valueOf(currency.toUpperCase());
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            List<Transaction> outliers = transactionRepository.findRecentOutliers(cryptoCurrency, since);
            return ResponseEntity.ok(outliers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/transactions/{currency}/spam")
    public ResponseEntity<List<Transaction>> getSpamTransactions(
            @PathVariable String currency,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            Transaction.CryptoCurrency cryptoCurrency = Transaction.CryptoCurrency.valueOf(currency.toUpperCase());
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            List<Transaction> spamTxs = transactionRepository.findRecentSpamTransactions(cryptoCurrency, since);
            return ResponseEntity.ok(spamTxs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/blocks/{currency}")
    public ResponseEntity<List<BlockStats>> getRecentBlocks(
            @PathVariable String currency,
            @RequestParam(defaultValue = "24") int hours) {
        try {
            Transaction.CryptoCurrency cryptoCurrency = Transaction.CryptoCurrency.valueOf(currency.toUpperCase());
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            List<BlockStats> blocks = blockStatsRepository.findRecentBlockStats(cryptoCurrency, since);
            return ResponseEntity.ok(blocks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/blocks/{currency}/fee-wars")
    public ResponseEntity<List<BlockStats>> getFeeWarBlocks(
            @PathVariable String currency,
            @RequestParam(defaultValue = "168") int hours) { // Default 1 week
        try {
            Transaction.CryptoCurrency cryptoCurrency = Transaction.CryptoCurrency.valueOf(currency.toUpperCase());
            LocalDateTime since = LocalDateTime.now().minusHours(hours);
            List<BlockStats> feeWarBlocks = blockStatsRepository.findRecentFeeWars(cryptoCurrency, since);
            return ResponseEntity.ok(feeWarBlocks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/transactions/{currency}/range")
    public ResponseEntity<List<Transaction>> getTransactionsByDateRange(
            @PathVariable String currency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            Transaction.CryptoCurrency cryptoCurrency = Transaction.CryptoCurrency.valueOf(currency.toUpperCase());
            List<Transaction> transactions = transactionRepository.findByCurrencyAndTimestampBetween(
                cryptoCurrency, start, end);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/blocks/{currency}/range")
    public ResponseEntity<List<BlockStats>> getBlocksByDateRange(
            @PathVariable String currency,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            Transaction.CryptoCurrency cryptoCurrency = Transaction.CryptoCurrency.valueOf(currency.toUpperCase());
            List<BlockStats> blocks = blockStatsRepository.findByCurrencyAndTimestampBetween(
                cryptoCurrency, start, end);
            return ResponseEntity.ok(blocks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Fee Market Comparator is running");
    }
}