package com.guarda.zcash.sapling.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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

    @Query("SELECT * FROM received_notes WHERE spent is NULL")
    List<ReceivedNotesRoom> getUnspents();

    @Query("SELECT * FROM received_notes WHERE cm LIKE :cm")
    ReceivedNotesRoom getNoteByCm(String cm);

    @Query("SELECT * FROM received_notes WHERE nf LIKE :nf")
    ReceivedNotesRoom getNoteByNf(String nf);

    @Query("SELECT nf FROM received_notes")
    List<String> getAllNf();

    @Query("UPDATE received_notes SET spent = 1 WHERE nf LIKE :nf")
    void spentNoteByNf(String nf);

    @Query("SELECT SUM(value) FROM received_notes WHERE spent IS NULL")
    Long getBalance();

    @Query("SELECT memo " +
            "FROM received_notes rn " +
            "LEFT join txins ins on ins.nf = rn.nf " +
            "LEFT join txouts outs on outs.cmu = rn.cm " +
            "WHERE ins.txHash like :hash or outs.txHash like :hash")
    String getMemoByHash(String hash);

    @Query("SELECT COUNT(*) " +
            "FROM blocks WHERE hash in " +
            "(SELECT blockHash FROM txs WHERE hash in " +
            "(SELECT txHash FROM txins WHERE nf in " +
            "(SELECT nf FROM received_notes))) " +
            "AND hash = :blockHash")
    int blockTxsByInputNote(String blockHash);

    @Query("SELECT COUNT(*) " +
            "FROM blocks WHERE hash in " +
            "(SELECT blockHash FROM txs WHERE hash in " +
            "(SELECT txHash FROM txouts WHERE cmu in " +
            "(SELECT cm FROM received_notes))) " +
            "AND hash = :blockHash")
    int blockTxsByOutputNote(String blockHash);

    @Query("DELETE FROM received_notes")
    void dropAll();

}
