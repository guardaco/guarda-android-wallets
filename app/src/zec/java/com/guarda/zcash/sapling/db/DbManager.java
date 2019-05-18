package com.guarda.zcash.sapling.db;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.guarda.ethereum.GuardaApp;
import com.guarda.zcash.crypto.Utils;
//import cash.z.wallet.sdk.rpc.CompactFormats;
import com.guarda.zcash.sapling.db.model.BlockRoom;
import com.guarda.zcash.sapling.db.model.TxInRoom;
import com.guarda.zcash.sapling.db.model.TxOutRoom;
import com.guarda.zcash.sapling.db.model.TxRoom;

import java.util.List;

import autodagger.AutoInjector;
import cash.z.wallet.sdk.rpc.CompactFormats;
import timber.log.Timber;

@AutoInjector(GuardaApp.class)
public class DbManager {

    private AppDb appDb;
    private final String DB_NAME = "lightwalletd";

    public DbManager(Context context) {
        appDb = Room.databaseBuilder(context, AppDb.class, DB_NAME)
                .build();
    }

    public void addBlockWithTxs(CompactFormats.CompactBlock cb) {
        Timber.d("addBlockWithTxs vb=%s", Utils.revHex(cb.getHash().toByteArray()));
        appDb.getBlockDao().insertAll(new BlockRoom(Utils.revHex(cb.getHash().toByteArray()), cb.getHeight()));
        for (CompactFormats.CompactTx ctx : cb.getVtxList()) {
            appDb.getTxDao().insertAll(new TxRoom(Utils.revHex(ctx.getHash().toByteArray()),
                    Utils.revHex(cb.getHash().toByteArray())));
            List<CompactFormats.CompactSpend> spends = ctx.getSpendsList();
            for (int i = 0; i < spends.size(); i++) {
                CompactFormats.CompactSpend cs = spends.get(i);
                //an id of a spend is txhash + i
                appDb.getTxInputDao().insertAll(new TxInRoom(Utils.revHex(ctx.getHash().toByteArray()) + i,
                        Utils.revHex(ctx.getHash().toByteArray()),
                        Utils.revHex(cs.getNf().toByteArray())));
            }

            List<CompactFormats.CompactOutput> outputs = ctx.getOutputsList();
            for (int i = 0; i < outputs.size(); i++) {
                CompactFormats.CompactOutput co = outputs.get(i);
                //an id of a output is txhash + i
                appDb.getTxOutputDao().insertAll(new TxOutRoom(Utils.revHex(ctx.getHash().toByteArray()) + i,
                        Utils.revHex(ctx.getHash().toByteArray()),
                        Utils.revHex(co.getCmu().toByteArray()),
                        Utils.revHex(co.getEpk().toByteArray()),
                        // Don't know why, but encCiphertext doesn't need reverse bytes
                        // Only first 52 bytes (https://github.com/gtank/zips/blob/light_payment_detection/zip-XXX-light-payment-detection.rst#output-compression)
                        Utils.bytesToHex(co.getCiphertext().toByteArray())));
            }
        }
    }

    public AppDb getAppDb() {
        return appDb;
    }
}
