package com.guarda.zcash.sapling.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.guarda.zcash.sapling.db.model.SaplingWitnessesRoom;

import java.util.List;


@Dao
public interface SaplingWitnessesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(SaplingWitnessesRoom... notes);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SaplingWitnessesRoom swr);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(List<SaplingWitnessesRoom> list);

    @Delete
    void delete(SaplingWitnessesRoom note);

    @Query("SELECT * FROM sapling_witnesses")
    List<SaplingWitnessesRoom> getAllWitnesses();

    @Query("SELECT * FROM sapling_witnesses WHERE cm LIKE :cm")
    SaplingWitnessesRoom getWitness(String cm);

    @Query("SELECT witnessHeight FROM sapling_witnesses order by witnessHeight desc LIMIT 1")
    Long getLastHeight();

    @Query("DELETE FROM sapling_witnesses WHERE witnessHeight LIKE :height")
    void deleteHeight(Long height);

    @Query("DELETE FROM sapling_witnesses")
    void dropAll();

}
