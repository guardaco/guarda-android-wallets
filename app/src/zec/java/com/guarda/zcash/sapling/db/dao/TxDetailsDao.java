package com.guarda.zcash.sapling.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.guarda.zcash.sapling.db.model.TxDetailsRoom;

import java.util.List;

@Dao
public interface TxDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(TxDetailsRoom... txDetailsRooms);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<TxDetailsRoom> txDetailsRooms);

    @Query("SELECT * FROM tx_details order by time desc")
    List<TxDetailsRoom> getTxDetailsListOrdered();

    @Query("SELECT * FROM tx_details where hash is :hash")
    TxDetailsRoom getTxDetails(String hash);

    @Delete
    void delete(TxDetailsRoom detailsTxRoom);

    @Query("DELETE FROM tx_details")
    void dropAll();

}
