package com.blockchain.blockpulseservice.tx;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapper {
    
    private static final BigDecimal SATOSHI_TO_BTC = new BigDecimal("100000000");
    
    public Transaction mapToTransaction(BlockchainInfoTransactionDto dto) {
        if (dto == null) {
            return null;
        }
        
        List<TransactionInput> inputs = mapInputs(dto.getInputs());
        List<TransactionOutput> outputs = mapOutputs(dto.getOutputs());
        
        BigDecimal totalValue = calculateTotalOutputValue(outputs);
        BigDecimal fee = dto.getFee() != null ? 
            new BigDecimal(dto.getFee()).divide(SATOSHI_TO_BTC) : BigDecimal.ZERO;
        
        return Transaction.builder()
            .hash(dto.getHash())
            .blockHeight(dto.getBlockHeight())
            .timestamp(dto.getTime() != null ? Instant.ofEpochSecond(dto.getTime()) : null)
            .totalValue(totalValue)
            .fee(fee)
            .inputCount(inputs != null ? inputs.size() : 0)
            .outputCount(outputs != null ? outputs.size() : 0)
            .inputs(inputs)
            .outputs(outputs)
            .size(dto.getSize())
            .virtualSize(dto.getVirtualSize())
            .confirmed(dto.getBlockHeight() != null && dto.getBlockHeight() > 0)
            .build();
    }
    
    private List<TransactionInput> mapInputs(List<BlockchainInfoInputDto> inputDtos) {
        if (inputDtos == null) {
            return null;
        }
        
        return inputDtos.stream()
            .map(this::mapInput)
            .collect(Collectors.toList());
    }
    
    private TransactionInput mapInput(BlockchainInfoInputDto inputDto) {
        if (inputDto == null) {
            return null;
        }
        
        BlockchainInfoPrevOutDto prevOut = inputDto.getPrevOut();
        
        return TransactionInput.builder()
            .previousTransactionHash(prevOut != null ? prevOut.getHash() : null)
            .previousOutputIndex(prevOut != null ? prevOut.getN() : null)
            .scriptSignature(inputDto.getScript())
            .address(prevOut != null ? prevOut.getAddress() : null)
            .value(prevOut != null && prevOut.getValue() != null ? 
                new BigDecimal(prevOut.getValue()).divide(SATOSHI_TO_BTC) : null)
            .sequence(inputDto.getSequence())
            .build();
    }
    
    private List<TransactionOutput> mapOutputs(List<BlockchainInfoOutputDto> outputDtos) {
        if (outputDtos == null) {
            return null;
        }
        
        return outputDtos.stream()
            .map(this::mapOutput)
            .collect(Collectors.toList());
    }
    
    private TransactionOutput mapOutput(BlockchainInfoOutputDto outputDto) {
        if (outputDto == null) {
            return null;
        }
        
        return TransactionOutput.builder()
            .index(outputDto.getN())
            .value(outputDto.getValue() != null ? 
                new BigDecimal(outputDto.getValue()).divide(SATOSHI_TO_BTC) : null)
            .scriptPubKey(outputDto.getScript())
            .address(outputDto.getAddress())
            .spent(outputDto.getSpent())
            .build();
    }
    
    private BigDecimal calculateTotalOutputValue(List<TransactionOutput> outputs) {
        if (outputs == null) {
            return BigDecimal.ZERO;
        }
        
        return outputs.stream()
            .filter(output -> output.getValue() != null)
            .map(TransactionOutput::getValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}