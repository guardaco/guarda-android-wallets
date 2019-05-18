package com.guarda.zcash.sapling.note;

public class SaplingOutPoint {
    public String hashIn;
    public int nIn;

    public SaplingOutPoint(String hashIn, int nIn) {
        this.hashIn = hashIn;
        this.nIn = nIn;
    }

    @Override
    public int hashCode() {
        return nIn + hashIn.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) return false;
        return (this.hashIn.hashCode() + this.nIn) == (((SaplingOutPoint) obj).hashIn.hashCode() + ((SaplingOutPoint) obj).nIn);
    }

    @Override
    public String toString() {
        return "SaplingOutPoint{" +
                "hashIn='" + hashIn + '\'' +
                ", nIn=" + nIn +
                '}';
    }
}
