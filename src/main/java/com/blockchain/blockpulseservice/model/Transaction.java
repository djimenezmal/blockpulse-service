package com.blockchain.blockpulseservice.model;

import java.util.Objects;

public record Transaction(String hash, long feeRate, int size, long time) implements Comparable<Transaction> {
    @Override
    public int compareTo(Transaction other) {
        int cmp = Double.compare(this.feeRate, other.feeRate);
        return cmp == 0 ? this.hash.compareTo(other.hash()) : cmp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction that)) return false;
        return this.hash.equals(that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}