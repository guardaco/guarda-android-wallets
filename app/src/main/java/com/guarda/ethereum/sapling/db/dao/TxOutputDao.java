package com.guarda.ethereum.sapling.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.guarda.ethereum.sapling.db.model.TxOutRoom;

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

    @Query("DELETE FROM txouts")
    void dropAll();
}
