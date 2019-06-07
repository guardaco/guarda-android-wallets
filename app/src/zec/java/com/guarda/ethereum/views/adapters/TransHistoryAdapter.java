package com.guarda.ethereum.views.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
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

    public static final int MIN_CONFIRMATIONS = 1;
    private OnItemClickListener listener;

    private List<TransactionItem> transList;
    private List<TransactionItem> pendingList;
    private View view;

    public TransHistoryAdapter() {
        GuardaApp.getAppComponent().inject(this);
        this.transList = transactionsManager.getTransactionsList();
        transactionsManager.clearDuplicateTransactions();
        this.pendingList = transactionsManager.getPendingTransactions();
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
                    listener.OnItemClick(position);
                }
        });

        if (item.getConfirmations() < MIN_CONFIRMATIONS) {
            holder.tvTxStatus.setText(R.string.tx_status_wait);
        } else {
            holder.tvTxStatus.setVisibility(View.GONE);
            holder.viewIndicator.setBackground(view.getContext().getResources().getDrawable(R.drawable.transaction_indicator_green));
            holder.viewIndicator.setVisibility(View.VISIBLE);
        }

        Coin coin = Coin.valueOf(txSum);
        String sumStr = coin.toPlainString() + " " + sharedManager.getCurrentCurrency().toUpperCase();
        holder.tvTransactionSum.setText(item.isOut() ? "-" + sumStr : sumStr);
        holder.tvDate.setText(CalendarHelper.parseDateToddMMyyyy(item.getTime() * 1000));

    }


    private TransactionItem getTxByPosition(int position) {
        if (position + 1 <= pendingList.size()) {
            return pendingList.get(position);
        } else {
            return transList.get(position - pendingList.size());
        }
    }

    public void updateList(List<TransactionItem> list) {
        this.transList = list;
    }

    private boolean isDebit(String ourAddress, String toAddress) {
        return ourAddress.equals(toAddress);
    }

    @Override
    public int getItemCount() {
        return transList.size() + pendingList.size();
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
}

