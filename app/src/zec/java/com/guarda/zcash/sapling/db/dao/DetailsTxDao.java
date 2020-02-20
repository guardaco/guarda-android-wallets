package com.guarda.zcash.sapling.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.guarda.zcash.sapling.db.model.DetailsTxRoom;

import java.util.List;

@Dao
public interface DetailsTxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(DetailsTxRoom... detailsTxRooms);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<DetailsTxRoom> detailsTxRooms);

    @Query("SELECT * FROM details_tx order by time desc")
    List<DetailsTxRoom> getTxDetailsListOrdered();

    @Query("SELECT * FROM details_tx where hash is :hash")
    DetailsTxRoom getTxDetails(String hash);

    @Delete
    void delete(DetailsTxRoom detailsTxRoom);

    @Query("DELETE FROM details_tx")
    void dropAll();

}
