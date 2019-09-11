package com.guarda.zcash.sapling.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

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
