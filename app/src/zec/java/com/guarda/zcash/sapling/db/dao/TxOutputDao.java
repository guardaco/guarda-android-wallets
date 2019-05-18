package com.guarda.zcash.sapling.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;

import com.guarda.zcash.sapling.db.model.TxOutRoom;

@Dao
public interface TxOutputDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(TxOutRoom... people);

    @Delete
    void delete(TxOutRoom person);
}
