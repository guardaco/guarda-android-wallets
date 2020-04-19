package com.guarda.zcash.sapling.rxcall;

import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.items.OutputDescs;
import com.guarda.ethereum.models.items.ZecTxResponse;
import com.guarda.zcash.sapling.db.model.TxOutRoom;
import com.guarda.zcash.sapling.note.SaplingNotePlaintext;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Callable;

import timber.log.Timber;

import static com.guarda.zcash.sapling.note.SaplingNotePlaintext.tryNoteDecrypt;


public class CallGetMemo implements Callable<String> {

    private ZecTxResponse tx;
    private WalletManager walletManager;

    public CallGetMemo(ZecTxResponse tx, WalletManager walletManager) {
        this.tx = tx;
        this.walletManager = walletManager;
    }

    @Override
    public String call() throws Exception {
        Timber.d("started");

        List<OutputDescs> outputDescs = tx.getOutputDescs();

        if (outputDescs.isEmpty()) return "";

        String memo = "";

        for (OutputDescs out : outputDescs) {
            TxOutRoom txOutRoom = new TxOutRoom("", tx.getHash(), out.getCmu(), out.getEphemeralKey(), out.getEncCiphertext());

            SaplingNotePlaintext snp = tryNoteDecrypt(txOutRoom, walletManager.getSaplingCustomFullKey());
            if (snp == null) continue;

            memo = new String(snp.getMemobytes());
            Timber.d("memo=%s", memo);
            return memo;
        }

        Timber.d("memo=%s", memo);

        return memo;
    }

}
