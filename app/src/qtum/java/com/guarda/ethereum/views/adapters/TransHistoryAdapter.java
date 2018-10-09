package com.guarda.ethereum.views.adapters;


import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.items.TokenRequestModel;
import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.ethereum.utils.CalendarHelper;
import com.guarda.ethereum.utils.RepeatHandler;

import org.bitcoinj.core.Coin;

import java.math.BigInteger;
import java.util.List;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.ButterKnife;

@AutoInjector(GuardaApp.class)
public class TransHistoryAdapter extends RecyclerView.Adapter<TransHistoryAdapter.TransHistoryItemHolder> {

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    @Inject
    WalletManager walletManager;

    @Inject
    TransactionsManager transactionsManager;

    @Inject
    SharedManager sharedManager;

    @Inject
    Context context;

    public static final int MIN_CONFIRMATIONS = 3;
    private ObjectAnimator loaderAnimation;
    private OnItemClickListener listener;

    private List<TransactionItem> transList;
    private List<TransactionItem> pendingList;
    private View view;
    private RepeatHandler repeatHandler;
    private List<TokenRequestModel> tokensToRequest;

    public TransHistoryAdapter(List<TokenRequestModel> tokensToRequest) {
        GuardaApp.getAppComponent().inject(this);
        this.transList = transactionsManager.getTransactionsList();
        transactionsManager.clearDuplicateTransactions();
        this.pendingList = transactionsManager.getPendingTransactions();
        this.tokensToRequest = tokensToRequest;
    }


    public void setItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    @Override
    public TransHistoryItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction_history, parent, false);
        return new TransHistoryItemHolder(view);
    }

    @Override
    public void onBindViewHolder(TransHistoryItemHolder holder, final int position) {
        final TransactionItem item = getTxByPosition(position);
        Long txSum = item.getValue();
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.OnItemClick(position);
                }
            }
        });

        if (item.getConfirmations() < MIN_CONFIRMATIONS) {
//            enableLoader(holder);
            holder.tvTxStatus.setText(R.string.tx_status_wait);
        } else {
//            disableLoader(holder);
            holder.tvTxStatus.setVisibility(View.GONE);
            holder.viewIndicator.setBackground(view.getContext().getResources().getDrawable(R.drawable.transaction_indicator_green));
            holder.viewIndicator.setVisibility(View.VISIBLE);
        }

        Coin coin = Coin.valueOf(txSum);
        //String sumStr = coin.toPlainString() + " " + sharedManager.getCurrentCurrency().toUpperCase();

        //for tokens where decimals not equals default value = 8
        int shift = getDecimal(item, tokensToRequest) - 8;
        if (shift != 0) {
            BigInteger bi = BigInteger.TEN.pow(shift < 0 ? shift * -1 : shift);
            coin = shift < 0 ? coin.multiply(bi.longValue()) : coin.divide(bi.longValue());
        }

        String sumStr = coin.toPlainString();

        holder.tvTransactionSum.setText(item.isOut() ? "-" + sumStr + " " + item.getTokenTicker() : sumStr + " " + item.getTokenTicker());
        holder.tvDate.setText(CalendarHelper.parseDateToddMMyyyy(item.getTime() * 1000));

//        if (!item.isOut()) {
//            holder.viewIndicator.setBackground(view.getContext().getResources().getDrawable(R.drawable.transaction_indicator_green));
//        } else {
//            holder.viewIndicator.setBackground(view.getContext().getResources().getDrawable(R.drawable.transaction_indicator_red));
//        }

//        holder.tvTokenTicker.setText(item.getTokenTicker());
    }


    private TransactionItem getTxByPosition(int position) {
        if (position + 1 <= pendingList.size()) {
            return pendingList.get(position);
        } else {
            return transList.get(position - pendingList.size());
        }
    }

    public static int getDecimal(TransactionItem ti, List<TokenRequestModel> tokensToRequest) {
        if (!ti.getTokenTicker().equalsIgnoreCase("QTUM")) {
            for (TokenRequestModel trm : tokensToRequest) {
                if (!trm.name.equalsIgnoreCase(ti.getTokenTicker())) continue;
                return trm.decimals;
            }
        }

        return 8;
    }


//    private void enableLoader(TransHistoryItemHolder holder) {
//        holder.ivLoader.setVisibility(View.VISIBLE);
//        holder.ivLoaderOuter.setVisibility(View.VISIBLE);
//        holder.tvDate.setVisibility(View.INVISIBLE);
//        holder.ivRetry.setVisibility(View.INVISIBLE);
//        startClockwiseRotation(holder.ivLoader);
//        startCounterclockwiseRotation(holder.ivLoaderOuter);
//    }
//
//    private void disableLoader(TransHistoryItemHolder holder) {
//        holder.ivLoader.setVisibility(View.INVISIBLE);
//        holder.ivLoaderOuter.setVisibility(View.INVISIBLE);
//        holder.tvDate.setVisibility(View.VISIBLE);
//        holder.ivRetry.setVisibility(View.INVISIBLE);
//    }


    private void startClockwiseRotation(ImageView ivLoader) {
        loaderAnimation = ObjectAnimator.ofFloat(ivLoader, "rotation", 0.0f, 360f);
        loaderAnimation.setDuration(1500);
        loaderAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        loaderAnimation.setInterpolator(new LinearInterpolator());
        loaderAnimation.start();
    }

    private void startCounterclockwiseRotation(ImageView ivLoader) {
        loaderAnimation = ObjectAnimator.ofFloat(ivLoader, "rotation", 360f, 0.0f);
        loaderAnimation.setDuration(1500);
        loaderAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        loaderAnimation.setInterpolator(new LinearInterpolator());
        loaderAnimation.start();
    }

    private boolean isDebit(String ourAddress, String toAddress) {
        return ourAddress.equals(toAddress);
    }

    @Override
    public int getItemCount() {
        return transList.size() + pendingList.size();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (repeatHandler != null) {
            repeatHandler.interrupt();
        }
    }

    class TransHistoryItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.main_view)
        View rootView;
        @BindView(R.id.indicator)
        View viewIndicator;
        @BindView(R.id.tv_transaction_date)
        TextView tvDate;
        @BindView(R.id.tv_transaction_sum)
        TextView tvTransactionSum;
        @BindView(R.id.tv_tx_status)
        TextView tvTxStatus;
//        @BindView(R.id.iv_loader)
//        ImageView ivLoader;
//        @BindView(R.id.ic_outer_loader)
//        ImageView ivLoaderOuter;
//        @BindView(R.id.iv_retry)
//        ImageView ivRetry;
//        @BindView(R.id.tv_token_ticker)
//        TextView tvTokenTicker;


        TransHistoryItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
//            tvTokenTicker.setVisibility(View.VISIBLE);
        }
    }
}

