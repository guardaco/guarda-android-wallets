package com.guarda.zcash.sapling.note;

import com.guarda.zcash.sapling.tree.IncrementalWitness;

import java.util.ArrayList;
import java.util.List;

public class SaplingNoteData {

    /**
     * from wallet.h:
     *
     * Block height corresponding to the most current witness.
     *
     * When we first create a SproutNoteData in CWallet::FindMySproutNotes, this is set to
     * -1 as a placeholder. The next time CWallet::ChainTip is called, we can
     * determine what height the witness cache for this note is valid for (even
     * if no witnesses were cached), and so can set the correct value in
     * CWallet::IncrementNoteWitnesses and CWallet::DecrementNoteWitnesses.
     */

    private byte[] hashIn;
    private long nIn;
    private List<IncrementalWitness> wintesses = new ArrayList<>();
    private long wintessHeight = -1;
    private String nullifier = "";

    public SaplingNoteData() {
    }

    public SaplingNoteData(byte[] hashIn, long nIn) {
        this.hashIn = hashIn;
        this.nIn = nIn;
    }

    public List<IncrementalWitness> getWintesses() {
        return wintesses;
    }

    public long getWintessHeight() {
        return wintessHeight;
    }

    public void setWintessHeight(long wintessHeight) {
        this.wintessHeight = wintessHeight;
    }

    public String getNullifier() {
        return nullifier;
    }

    public void setNullifier(String nullifier) {
        this.nullifier = nullifier;
    }
}
