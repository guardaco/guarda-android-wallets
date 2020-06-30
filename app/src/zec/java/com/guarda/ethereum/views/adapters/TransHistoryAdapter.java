package com.guarda.ethereum.views.adapters;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;
import com.guarda.ethereum.managers.TransactionsManager;
import com.guarda.ethereum.managers.WalletManager;
import com.guarda.ethereum.models.items.TransactionItem;
import com.guarda.ethereum.utils.CalendarHelper;

import org.bitcoinj.core.Coin;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransHistoryAdapter extends RecyclerView.Adapter<TransHistoryAdapter.TransHistoryItemHolder> {

    @Inject
    WalletManager walletManager;
    @Inject
    TransactionsManager transactionsManager;
    @Inject
    SharedManager sharedManager;
    @Inject
    Context context;

    public static final int MIN_CONFIRMATIONS = 1;
    private OnItemClickListener listener;

    private List<TransactionItem> transList;
    private View view;

    public TransHistoryAdapter() {
        GuardaApp.getAppComponent().inject(this);
        this.transList = transactionsManager.getTransactionsList();
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
        Long txSum = item.getSum();
        holder.rootView.setOnClickListener((v) -> {
                if (listener != null) {
                    listener.OnItemClick(item);
                }
        });

        if (item.getConfirmations() < MIN_CONFIRMATIONS) {
            holder.tvTxStatus.setVisibility(View.VISIBLE);
            holder.tvTxStatus.setText(R.string.tx_status_wait);
            holder.viewIndicator.setVisibility(View.GONE);
        } else {
            holder.tvTxStatus.setVisibility(View.GONE);
            holder.viewIndicator.setBackground(view.getContext().getResources().getDrawable(R.drawable.transaction_indicator_green));
            holder.viewIndicator.setVisibility(View.VISIBLE);
        }

        Coin coin = Coin.valueOf(txSum);
        String sumStr = coin.toPlainString() + " " + sharedManager.getCurrentCurrency().toUpperCase();
        holder.tvTransactionSum.setText(item.isOut() ? "-" + sumStr : sumStr);
        holder.tvDate.setText(CalendarHelper.parseDateToddMMyyyy(item.getTime() * 1000));

        if (item.getFrom().isEmpty() && item.getTo().isEmpty() && item.getSum() == 0L && item.getConfirmations() == 0L) {
            holder.tvTransactionSum.setText("");
            holder.tvTxStatus.setVisibility(View.VISIBLE);
            holder.tvTxStatus.setText(R.string.tx_status_syncing);
        }
    }

    private TransactionItem getTxByPosition(int position) {
        return transList.get(position);
    }

    public void updateList(List<TransactionItem> list) {
        this.transList = list;
    }

    @Override
    public int getItemCount() {
        return transList.size();
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

        TransHistoryItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(TransactionItem item);
    }
}

