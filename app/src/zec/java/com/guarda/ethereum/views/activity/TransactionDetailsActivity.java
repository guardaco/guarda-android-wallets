package com.guarda.ethereum.views.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.EthereumNetworkManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.models.constants.Extras;
import com.guarda.ethereum.models.items.OutputDescs;
import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.ethereum.models.items.ZecTxResponse;
import com.guarda.ethereum.rest.RequestorBtc;
import com.guarda.ethereum.utils.ClipboardUtils;
import com.guarda.ethereum.views.activity.base.AToolbarMenuActivity;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.note.SaplingNotePlaintext;
import com.guarda.zcash.sapling.rxcall.CallGetMemo;


import org.bitcoinj.core.Coin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.guarda.ethereum.models.constants.Common.EXTRA_TRANSACTION_POSITION;
import static com.guarda.ethereum.models.constants.ZecExplorer.ZEC_EXPLORER_TX;

@AutoInjector(GuardaApp.class)
public class TransactionDetailsActivity extends AToolbarMenuActivity {

    @BindView(R.id.et_trans_details_sum)
    EditText etTrValue;
    @BindView(R.id.et_trans_details_date)
    EditText etDate;
    @BindView(R.id.et_trans_details_time)
    EditText etTime;
    @BindView(R.id.et_trans_details_hash)
    EditText etHash;
    @BindView(R.id.et_trans_details_balance_after)
    EditText etBalanceAfter;
    @BindView(R.id.et_trans_details_balance_before)
    EditText etBalanceBefore;
    @BindView(R.id.btn_copy)
    Button btnCopy;
    @BindView(R.id.btn_repeat)
    Button btnRepeat;
    @BindView(R.id.tv_confirmations)
    TextView etConfirmations;
    @BindView(R.id.ll_memo)
    LinearLayout ll_memo;
    @BindView(R.id.et_memo)
    EditText et_memo;

    @Inject
    TransactionsManager transactionsManager;
    @Inject
    WalletManager walletManager;
    @Inject
    EthereumNetworkManager networkManager;
    @Inject
    DbManager dbManager;

    private TransactionItem transaction;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void init(Bundle savedInstanceState) {
        GuardaApp.getAppComponent().inject(this);
        setToolBarTitle(getString(R.string.title_transaction_detail));
        int transactionPosition = getIntent().getIntExtra(EXTRA_TRANSACTION_POSITION, 0);
        transaction = transactionsManager.getTxByPosition(transactionPosition);
        updateViews();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_transaction_details;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.no_slide, R.anim.slide_in_right);
    }

    @Override
    protected void onStop() {
        super.onStop();
        compositeDisposable.clear();
    }

    private void updateViews() {
        if (transaction != null) {
            setValue(transaction.isOut() ? -transaction.getSum() : transaction.getSum());
            setDateAndTime();
            setHash(transaction.getHash());
            setConfirmations(String.valueOf(transaction.getConfirmations()));
            setBalanceDetails();
            initRepeatButton();
            showMemo();
        }
    }

    private void initRepeatButton() {
        if (isDebit(walletManager.getWalletFriendlyAddress(), transaction.getTo())
                || transaction.getTo().equalsIgnoreCase(Common.ZCASH_JOIN_SPLIT)) {
            btnRepeat.setVisibility(View.GONE);
        } else {
            btnRepeat.setVisibility(View.VISIBLE);
        }
    }

    private void showMemo() {
        Observable getCmFromDb = Observable.fromCallable(new CallGetMemo(transaction.getHash(), dbManager));
        Observable getTxFromExplorer = RequestorBtc.getOneTx(transaction.getHash());
        compositeDisposable.add(getCmFromDb.zipWith(getTxFromExplorer, new BiFunction<String, ZecTxResponse, OutputDescs>() {
                    @Override
                    public OutputDescs apply(String cm, ZecTxResponse txResponse) throws Exception {
                        Timber.d("showMemo zipWith cm=%s tx=%s", cm, txResponse);
                        if (txResponse == null || cm == null) return null;
                        List<OutputDescs> outs = txResponse.getOutputDescs();
                        if (outs == null || outs.size() == 0) return null;
                        for (OutputDescs o : outs) {
                            if (o.getCmu() == cm) {
                                return o;
                            }
                        }
                        return null;
                    }
                }
        ).subscribe(out -> {
                    Timber.d("res=%s", out);
                    if (out == null) return;

            SaplingNotePlaintext.decrypt(out);
                }
        ));
//        compositeDisposable.add(
//                Observable.fromCallable(new CallGetMemo(transaction.getHash(), dbManager)).
//                RequestorBtc
//                        .getOneTx(transaction.getHash())
//                        .flatMap(zecTxResponse -> {
//                            String encCipher = "";
//                            if (zecTxResponse != null) {
//                                zecTxResponse.getOutputDescs()
//                            } else {
//                                Timber.d("zecTxResponse is null");
//                            }
//                            return Observable.just(encCipher);
//                        })
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(
//                                t -> {
//                                    Timber.d("dfdfd");
//                                },
//                                e -> Timber.d("showMemo e=%s", e.getMessage()))
//        );


//        compositeDisposable.add(
//                Observable.just(transaction.getHash())
//                        .flatMap(hash -> {
//                            Timber.d("showMemo hash=%s", hash);
//                            return Observable.fromCallable(new CallGetMemo(hash, dbManager));
//                        })
//                        .flatMap(txCm -> {
//                            String cm = txCm.get();
//                            Timber.d("CallGetMemo cm=%s", cm);
//                            return Observable.just(cm);
//                        })
//                        .flatMap(str -> {
//
//                            return Observable.just("");
//                        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(mem -> {
//
//                        }
//                )
//                Observable
//                        .fromCallable(new CallGetMemo(transaction.getHash(), dbManager))
//                        .flatMap(txCm -> {
//                            String cm = txCm.get();
//                            Timber.d("CallGetMemo cm=%s", cm);
//                            if (cm == null) return;
//                            return Observable.just(cm);
//                        }).flatMap(
//
//                )
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(memo -> {
//                        })
//        );
    }

    @OnClick(R.id.btn_copy_memo)
    public void copyMemo(View view) {
        ClipboardUtils.copyToClipBoard(this, et_memo.getText().toString());
    }

    private void setBalanceDetails() {
        long balanceBefore = transactionsManager.getBalanceByTime(true, walletManager.getMyBalance().getValue(), transaction.getTime());
        long balanceAfter;
        if (balanceBefore == 0) {
            balanceAfter = getStartBalanceAfter(balanceBefore, transaction.getSum());
        } else {
            balanceAfter = transactionsManager.getBalanceByTime(false, walletManager.getMyBalance().getValue(), transaction.getTime());
        }
        etBalanceBefore.setText(String.format("%s %s", WalletManager.getFriendlyBalance(Coin.valueOf(balanceBefore)), Common.MAIN_CURRENCY.toUpperCase()));
        etBalanceAfter.setText(String.format("%s %s", WalletManager.getFriendlyBalance(Coin.valueOf(balanceAfter)), Common.MAIN_CURRENCY.toUpperCase()));
    }

    private long getStartBalanceAfter(long balanceBefore, long summ) {
        return balanceBefore + summ;
    }

    private void setConfirmations(String confirmations) {
        if (confirmations != null) {
            etConfirmations.setText(confirmations);
        } else {
            etConfirmations.setText(String.format(Locale.US, "%d", 0));
        }
    }

    private void setHash(String hash) {
        etHash.setText(hash);
    }

    private void setDateAndTime() {
        Date date = new Date(transaction.getTime() * 1000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        etDate.setText(dateFormat.format(date));
        etTime.setText(timeFormat.format(date));
    }

    private void setValue(long value) {
        Coin coin = Coin.valueOf(value);
        etTrValue.setText(String.format("%s %s", WalletManager.getFriendlyBalance(coin), Common.MAIN_CURRENCY.toUpperCase()));
    }

    private boolean isDebit(String ourAddress, String toAddress) {
        return ourAddress.equals(toAddress);
    }

    @OnClick(R.id.btn_copy)
    public void onCopyClick() {
        ClipboardUtils.copyToClipBoard(this, transaction.getHash());
    }

    @OnClick(R.id.btn_repeat)
    public void onRepeatTransactionClick() {

        String trValue = etTrValue.getText().toString();
        if (trValue.charAt(0) == '-') {
            trValue = trValue.substring(1);
        }

        Intent intent = new Intent(this, SendingCurrencyActivity.class);
        intent.putExtra(Extras.WALLET_NUMBER, transaction.getTo());
        intent.putExtra(Extras.AMOUNT_TO_SEND, WalletManager.getFriendlyBalance(Coin.valueOf(transaction.getSum())));
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.no_slide);
    }

    @OnClick({R.id.til_trans_details_hash, R.id.et_trans_details_hash})
    public void onTransHashClick() {
        String hashDetailUrl = ZEC_EXPLORER_TX + etHash.getText().toString();
        openWebURL(hashDetailUrl);
    }

    public void openWebURL(String inURL) {
        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(inURL));
        startActivity(browse);
    }

}
