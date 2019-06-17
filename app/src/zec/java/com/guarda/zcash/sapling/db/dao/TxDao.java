package com.guarda.zcash.sapling.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.guarda.zcash.sapling.db.model.TxInRoom;
import com.guarda.zcash.sapling.db.model.TxOutRoom;
import com.guarda.zcash.sapling.db.model.TxRoom;

import java.util.List;

@Dao
public interface TxDao {
    @Query("SELECT * FROM txins where txHash is :hash")
    List<TxInRoom> getAllTxInputs(String hash);

    @Query("SELECT * FROM txouts where txHash is :hash")
    List<TxOutRoom> getAllTxOutputs(String hash);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(TxRoom... people);

    @Query("SELECT * FROM txs where blockHash is :bHash")
    List<TxRoom> getAllBlockTxs(String bHash);

    @Delete
    void delete(TxRoom person);

    @Query("DELETE FROM txs")
    void dropAll();
}
