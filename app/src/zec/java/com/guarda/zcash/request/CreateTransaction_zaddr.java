package com.guarda.zcash.request;

import com.guarda.zcash.WalletCallback;
import com.guarda.zcash.ZCashException;
import com.guarda.zcash.ZCashTransactionOutput;
import com.guarda.zcash.ZCashTransaction_zaddr;
import com.guarda.zcash.ZcashTransaction;
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
  private WalletCallback<String, ZcashTransaction> callback;
  private long fee;
  private long value;
  private String memo;
  private List<ZCashTransactionOutput> utxos;
  private int expiryHeight;

  private DbManager dbManager;
  private CompositeDisposable compositeDisposable = new CompositeDisposable();

  public CreateTransaction_zaddr(String fromAddr,
                                 String toAddr,
                                 long value,
                                 long fee,
                                 String memo,
                                 SaplingCustomFullKey privatekey,
                                 int expiryHeight,
                                 DbManager dbManager,
                                 WalletCallback<String, ZcashTransaction> callback) {
    this.fromAddr = fromAddr;
    this.toAddr = toAddr;
    this.value = value;
    this.fee = fee;
    this.memo = memo;
    this.privateKey = privatekey;
    this.expiryHeight = expiryHeight;
    this.dbManager = dbManager;
    this.callback = callback;
  }

  @Override
  public void run() {

    compositeDisposable.add(Observable
            .fromCallable(new CallBuildTransaction(dbManager, toAddr, value, fee, memo, privateKey, expiryHeight))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe((res) -> {
              Timber.d("CallBuildTransaction latest=%s", res);

              callback.onResponse("ok", res);
            }, (error) -> callback.onResponse(error.getMessage(), null)));

  }

}
