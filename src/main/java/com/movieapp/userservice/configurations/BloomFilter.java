package com.movieapp.userservice.configurations;

import java.util.BitSet;

public class BloomFilter {
    BitSet bits;
    int size;
    int numberOfHashes;

    public BloomFilter() {
        size = 10000;
        bits = new BitSet(size);
        numberOfHashes = 3;
    }

    public void add(String s) {
        for(int i=0;i<numberOfHashes;i++) {
            int position = hash(s,i);
            bits.set(position);
        }
    }

    public void remove(String s) {
        for(int i=0;i<numberOfHashes;i++) {
            int position = hash(s,i);
            bits.clear(position);
        }
    }

    public boolean mightContain(String s) {
        // check for all hashes
        for(int i=0;i<numberOfHashes;i++) {
            int position = hash(s,i);
            if(!bits.get(position)) return false; // definitely doesnt contain
        }
        return true;
    }


    private int hash(String value,int seed) {
        int hash = value.hashCode();
        hash ^= seed * 0x9e3779b9;
        hash ^= (hash >>> 16);
        return Math.floorMod(hash,size);
    }

    public static void main(String[] args) {
        BloomFilter bloom = new BloomFilter();
        System.out.println(bloom.mightContain("Dhanush"));
        bloom.add("Dhanush");
        System.out.println(bloom.mightContain("Dhanush"));

        System.out.println(bloom.mightContain("Dhanushch3"));
        bloom.add("Dhanushch3");
        System.out.println(bloom.mightContain("Dhanushch3"));

        System.out.println(bloom.mightContain("Dhanushch"));
        bloom.add("Dhanushch");
        System.out.println(bloom.mightContain("Dhanushch"));

        System.out.println(bloom.mightContain("Dhanushch31"));
        bloom.add("Dhanushch31");
        System.out.println(bloom.mightContain("Dhanushch31"));

        bloom.remove("Dhanushch31");
        System.out.println(bloom.mightContain("Dhanushch31"));
    }

}
