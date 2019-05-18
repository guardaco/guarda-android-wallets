package com.guarda.zcash.sapling.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.guarda.zcash.sapling.db.model.ReceivedNotesRoom;

import java.util.List;


@Dao
public interface ReceivedNotesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(ReceivedNotesRoom... notes);

    @Delete
    void delete(ReceivedNotesRoom note);

    @Query("SELECT * FROM received_notes")
    List<ReceivedNotesRoom> getAllNotes();

    @Query("SELECT * FROM received_notes WHERE cm LIKE :cm")
    ReceivedNotesRoom getNote(String cm);

}
