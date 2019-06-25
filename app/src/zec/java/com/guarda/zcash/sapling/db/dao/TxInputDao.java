package com.guarda.zcash.sapling.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.guarda.zcash.sapling.db.model.TxInRoom;

import java.util.List;

@Dao
public interface TxInputDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(TxInRoom... people);

    @Delete
    void delete(TxInRoom person);

    @Query("SELECT txHash FROM txins ins inner join received_notes nts on ins.nf = nts.nf")
    List<String> getInputTxIds();

    @Query("SELECT nf FROM txins WHERE txHash LIKE :hash")
    List<String> getNfByHash(String hash);

    @Query("DELETE FROM txins")
    void dropAll();
}
