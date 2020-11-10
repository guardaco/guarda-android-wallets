package com.guarda.ethereum.sapling.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.guarda.ethereum.sapling.db.dao.BlockDao;
import com.guarda.ethereum.sapling.db.dao.DetailsTxDao;
import com.guarda.ethereum.sapling.db.dao.ReceivedNotesDao;
import com.guarda.ethereum.sapling.db.dao.SaplingWitnessesDao;
import com.guarda.ethereum.sapling.db.dao.TxDao;
import com.guarda.ethereum.sapling.db.dao.TxDetailsDao;
import com.guarda.ethereum.sapling.db.dao.TxInputDao;
import com.guarda.ethereum.sapling.db.dao.TxOutputDao;
import com.guarda.ethereum.sapling.db.model.BlockRoom;
import com.guarda.ethereum.sapling.db.model.DetailsTxRoom;
import com.guarda.ethereum.sapling.db.model.ReceivedNotesRoom;
import com.guarda.ethereum.sapling.db.model.SaplingWitnessesRoom;
import com.guarda.ethereum.sapling.db.model.TxDetailsRoom;
import com.guarda.ethereum.sapling.db.model.TxInRoom;
import com.guarda.ethereum.sapling.db.model.TxOutRoom;
import com.guarda.ethereum.sapling.db.model.TxRoom;

@Database(entities = {
        BlockRoom.class,
        TxRoom.class,
        TxOutRoom.class,
        TxInRoom.class,
        ReceivedNotesRoom.class,
        SaplingWitnessesRoom.class,
        DetailsTxRoom.class,
        TxDetailsRoom.class
}, version = 3)
public abstract class AppDb extends RoomDatabase {
    public abstract BlockDao getBlockDao();
    public abstract TxDao getTxDao();
    public abstract TxInputDao getTxInputDao();
    public abstract TxOutputDao getTxOutputDao();

    public abstract ReceivedNotesDao getReceivedNotesDao();
    public abstract SaplingWitnessesDao getSaplingWitnessesDao();
    public abstract DetailsTxDao getDetailsTxDao();
    public abstract TxDetailsDao getTxDetailsDao();
}
