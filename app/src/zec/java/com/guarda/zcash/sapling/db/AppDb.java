package com.guarda.zcash.sapling.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.guarda.zcash.sapling.db.dao.BlockDao;
import com.guarda.zcash.sapling.db.dao.DetailsTxDao;
import com.guarda.zcash.sapling.db.dao.ReceivedNotesDao;
import com.guarda.zcash.sapling.db.dao.SaplingWitnessesDao;
import com.guarda.zcash.sapling.db.dao.TxDao;
import com.guarda.zcash.sapling.db.dao.TxInputDao;
import com.guarda.zcash.sapling.db.dao.TxOutputDao;
import com.guarda.zcash.sapling.db.model.BlockRoom;
import com.guarda.zcash.sapling.db.model.DetailsTxRoom;
import com.guarda.zcash.sapling.db.model.ReceivedNotesRoom;
import com.guarda.zcash.sapling.db.model.SaplingWitnessesRoom;
import com.guarda.zcash.sapling.db.model.TxInRoom;
import com.guarda.zcash.sapling.db.model.TxOutRoom;
import com.guarda.zcash.sapling.db.model.TxRoom;

@Database(entities = {
        BlockRoom.class,
        TxRoom.class,
        TxOutRoom.class,
        TxInRoom.class,
        ReceivedNotesRoom.class,
        SaplingWitnessesRoom.class,
        DetailsTxRoom.class
}, version = 1)
public abstract class AppDb extends RoomDatabase {
    public abstract BlockDao getBlockDao();
    public abstract TxDao getTxDao();
    public abstract TxInputDao getTxInputDao();
    public abstract TxOutputDao getTxOutputDao();

    public abstract ReceivedNotesDao getReceivedNotesDao();
    public abstract SaplingWitnessesDao getSaplingWitnessesDao();
    public abstract DetailsTxDao getDetailsTxDao();
}
