package com.guarda.zcash.sapling.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.guarda.zcash.sapling.db.model.BlockRoom;

import java.util.List;


@Dao
public interface BlockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(BlockRoom... people);

    @Delete
    void delete(BlockRoom person);

    @Query("SELECT * FROM blocks")
    List<BlockRoom> getAllBlocks();

    @Query("SELECT * FROM blocks order by height")
    List<BlockRoom> getAllBlocksOrdered();

    @Query("SELECT * FROM blocks WHERE hash LIKE :hash")
    BlockRoom getBlock(String hash);

}
