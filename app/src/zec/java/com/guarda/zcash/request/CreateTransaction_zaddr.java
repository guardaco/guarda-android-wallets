package com.guarda.zcash.request;

import com.guarda.zcash.WalletCallback;
import com.guarda.zcash.ZCashException;
import com.guarda.zcash.ZCashTransactionOutput;
import com.guarda.zcash.ZCashTransaction_zaddr;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.db.model.ReceivedNotesRoom;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;
import com.guarda.zcash.sapling.note.SpendProof;
import com.guarda.zcash.sapling.rxcall.CallBuildTransaction;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


public class CreateTransaction_zaddr extends AbstractZCashRequest implements Runnable {
  private String fromAddr;
  private String toAddr;
  private SaplingCustomFullKey privateKey;
  private WalletCallback<String, ZCashTransaction_zaddr> callback;
  private long fee;
  private long value;
  private List<ZCashTransactionOutput> utxos;
  private int expiryHeight;

  private DbManager dbManager;
  private CompositeDisposable compositeDisposable = new CompositeDisposable();

  public CreateTransaction_zaddr(String fromAddr,
                                 String toAddr,
                                 long value,
                                 long fee,
                                 SaplingCustomFullKey privatekey,
                                 int expiryHeight,
                                 DbManager dbManager,
                                 WalletCallback<String, ZCashTransaction_zaddr> callback) {
    this.fromAddr = fromAddr;
    this.toAddr = toAddr;
    this.value = value;
    this.fee = fee;
    this.privateKey = privatekey;
    this.expiryHeight = expiryHeight;
    this.dbManager = dbManager;
    this.callback = callback;
  }

  @Override
  public void run() {

    compositeDisposable.add(Observable
            .fromCallable(new CallBuildTransaction(dbManager, toAddr, value, fee, privateKey, expiryHeight))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe((res) -> {
              Timber.d("CallBuildTransaction latest=%s", res);

              callback.onResponse("ok", res);
            }, (error) -> callback.onResponse(error.getMessage(), null)));

  }

  private ZCashTransaction_zaddr createTransaction() throws ZCashException {
    List<ReceivedNotesRoom> listRnr = dbManager.getAppDb().getReceivedNotesDao().getAllNotes();
    Timber.d("listRnr s=%s", listRnr.size());

    List<ZCashTransactionOutput> outputs = new LinkedList<>();
    long realValue = chooseUTXOs(outputs);
    if (realValue < fee + value) {
      throw new ZCashException("Not enough balance.");
    }
//    TestScanBlocks tsb = new TestScanBlocks();
//    tsb.getWintesses();
//    SpendProof spendProof = tsb.addSpendS();
    SpendProof spendProof = new SpendProof(new byte[0], new byte[0], new byte[0], new byte[0], new byte[0], new byte[0]);

//    return new ZCashTransaction_zaddr(DumpedPrivateKey.fromBase58(privateKey), fromAddr, toAddr,
//            value, fee, expiryHeight, outputs, spendProof);
    return null;
  }


  private long chooseUTXOs(List<ZCashTransactionOutput> outputs) {
    long realValue = value + fee;
    long sum = 0;
    for (ZCashTransactionOutput out : utxos) {
      outputs.add(out);
      sum += out.value;
      if (sum >= realValue) {
        break;
      }

    }

    return sum;
  }
}
