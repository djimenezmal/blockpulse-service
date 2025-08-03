package com.blockchain.blockpulseservice.mapper;

import com.blockchain.blockpulseservice.dto.MempoolTransactionsDTOWrapper;
import com.blockchain.blockpulseservice.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionMapper {
    public List<Transaction> mapToTransaction(List<MempoolTransactionsDTOWrapper.TransactionDTO> transactionDTOS) {
        return transactionDTOS.stream()
                .map(t ->
                        new Transaction(t.id(), t.feePerVSize(), t.fee(), t.size(), t.firstSeen()))
                .toList();
    }
}