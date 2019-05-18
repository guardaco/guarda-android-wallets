package com.guarda.zcash.sapling.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;

import com.guarda.zcash.sapling.db.model.TxInRoom;

@Dao
public interface TxInputDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(TxInRoom... people);

    @Delete
    void delete(TxInRoom person);
}
