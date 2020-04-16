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
import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.ethereum.rest.RequestorBtc;
import com.guarda.ethereum.utils.ClipboardUtils;
import com.guarda.ethereum.views.activity.base.AToolbarMenuActivity;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.rxcall.CallGetMemo;


import org.bitcoinj.core.Coin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
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
    @BindView(R.id.ll_balances_before_after)
    LinearLayout ll_balances_before_after;

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
        transaction = transactionsManager.getTxDeatailsItem();
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
        ll_balances_before_after.setVisibility(View.GONE);

        if (transaction != null) {
            setValue();
            setDateAndTime();
            setHash(transaction.getHash());
            setConfirmations(String.valueOf(transaction.getConfirmations()));
            setBalanceDetails();
            initRepeatButton();
            showMemo();
        }
    }

    private void initRepeatButton() {
        btnRepeat.setVisibility(View.GONE);
    }

    private void showMemo() {
        compositeDisposable.add(
                RequestorBtc
                        .getOneTx(transaction.getHash())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(tx -> Observable.fromCallable(new CallGetMemo(tx, walletManager)))
                        .subscribe(memo -> {
                                    Timber.d("CallGetMemo memo=%s", memo);

                                    if (memo == null) return;
                                    if (memo.isEmpty()) return;

                                    ll_memo.setVisibility(View.VISIBLE);
                                    et_memo.setText(memo);
                        }, (error) -> Timber.e("showMemo %s", error.getMessage()))
        );
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

    private void setValue() {
        if (transaction.getFrom().isEmpty() && transaction.getTo().isEmpty() &&
                transaction.getSum() == 0L && transaction.getConfirmations() == 0L) {
            //for z transactions which have syncing status
            etTrValue.setText(R.string.tx_status_syncing);
        } else {
            Coin coin = Coin.valueOf(transaction.isOut() ? -transaction.getSum() : transaction.getSum());
            etTrValue.setText(String.format("%s %s", WalletManager.getFriendlyBalance(coin), Common.MAIN_CURRENCY.toUpperCase()));
        }
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
