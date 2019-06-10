package com.guarda.zcash.sapling.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.guarda.zcash.sapling.db.model.TxOutRoom;

import java.util.List;

@Dao
public interface TxOutputDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(TxOutRoom... people);

    @Delete
    void delete(TxOutRoom person);

    @Query("SELECT cmu FROM txouts WHERE txHash LIKE :hash")
    List<String> getCmByHash(String hash);

    @Query("SELECT * FROM txouts WHERE cmu LIKE :cmu")
    TxOutRoom getOut(String cmu);

    @Query("SELECT txHash FROM txouts ots inner join received_notes nts on ots.cmu = nts.cm")
    List<String> getOutputTxIds();
}
