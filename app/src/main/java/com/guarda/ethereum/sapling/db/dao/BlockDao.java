package com.guarda.ethereum.sapling.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.guarda.ethereum.sapling.db.model.BlockRoom;

import java.util.List;


@Dao
public interface BlockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(BlockRoom... blockRooms);

    @Update
    void update(BlockRoom blockRoom);

    @Delete
    void delete(BlockRoom blockRoom);

    @Query("SELECT * FROM blocks WHERE height > :height order by height")
    List<BlockRoom> getBlocksOrderedFromHeight(long height);

    @Query("SELECT * FROM blocks WHERE hash LIKE :hash")
    BlockRoom getBlock(String hash);

    @Query("SELECT * FROM blocks order by height DESC LIMIT 1")
    BlockRoom getLatestBlock();

    @Query("SELECT * FROM blocks WHERE height < :blockHeight order by height DESC LIMIT 1")
    BlockRoom previousBlock(long blockHeight);

    @Query("SELECT * FROM blocks WHERE tree <> '' AND tree IS NOT NULL order by height DESC LIMIT 1")
    BlockRoom getLatestBlockWithTree();

    @Query("UPDATE blocks SET tree = :tree WHERE height = :height")
    void setTreeByHeight(String tree, Long height);

    @Query("DELETE FROM blocks WHERE height = :blockHeight")
    void deleteByHeight(long blockHeight);

    @Query("DELETE FROM blocks WHERE height = (SELECT height FROM blocks WHERE tree <> '' AND tree IS NOT NULL order by height DESC LIMIT 1)")
    void deleteLastWithThree();

    @Query("UPDATE blocks SET tree = '' WHERE tree <> ''")
    void dropAllTrees();

    @Query("DELETE FROM blocks")
    void dropAll();

}
