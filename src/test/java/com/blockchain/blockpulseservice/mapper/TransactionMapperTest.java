package com.blockchain.blockpulseservice.mapper;

import com.blockchain.blockpulseservice.model.Transaction;
import com.blockchain.blockpulseservice.model.dto.MempoolTransactionsDTOWrapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionMapperTest {

    private final TransactionMapper mapper = new TransactionMapper();

    @Test
    void mapToTransaction_shouldMapAllFields() {
        Instant now = Instant.now();
        List<MempoolTransactionsDTOWrapper.TransactionDTO> dtos = List.of(
                new MempoolTransactionsDTOWrapper.TransactionDTO("hash1", 123, BigDecimal.TEN, BigDecimal.ONE, now),
                new MempoolTransactionsDTOWrapper.TransactionDTO("hash2", 456, BigDecimal.ONE, new BigDecimal("0.5"), now.plusSeconds(1))
        );

        List<Transaction> result = mapper.mapToTransaction(dtos);

        assertThat(result).hasSize(2);
        Transaction first = result.get(0);
        assertThat(first.hash()).isEqualTo("hash1");
        assertThat(first.vSize()).isEqualTo(123);
        assertThat(first.totalFee()).isEqualTo(BigDecimal.TEN);
        assertThat(first.feePerVSize()).isEqualTo(BigDecimal.ONE);
        assertThat(first.time()).isEqualTo(now);

        Transaction second = result.get(1);
        assertThat(second.hash()).isEqualTo("hash2");
        assertThat(second.vSize()).isEqualTo(456);
        assertThat(second.totalFee()).isEqualTo(BigDecimal.ONE);
        assertThat(second.feePerVSize()).isEqualTo(new BigDecimal("0.5"));
        assertThat(second.time()).isEqualTo(now.plusSeconds(1));
    }

    @Test
    void mapToTransaction_withEmptyListReturnsEmpty() {
        List<MempoolTransactionsDTOWrapper.TransactionDTO> dtos = List.of();

        List<Transaction> result = mapper.mapToTransaction(dtos);

        assertThat(result).isEmpty();
    }
}

