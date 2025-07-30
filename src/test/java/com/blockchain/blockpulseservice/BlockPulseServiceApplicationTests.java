package com.blockchain.blockpulseservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.TreeMap;

//@SpringBootTest
class BlockPulseServiceApplicationTests {

    @Test
    void contextLoads() {
        TreeMap<Integer, Integer> map = new TreeMap<>();
        map.pollFirstEntry();
        map.put(5,5);
        map.put(1,1);
        map.put(6,6);
        map.put(4,4);
        map.put(3,3);
        map.put(2,2);

        map.forEach((k,v) -> System.out.println(k + " " + v));
    }
}