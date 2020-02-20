package com.guarda.ethereum.views.adapters;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guarda.ethereum.R;
import com.guarda.ethereum.models.items.CoinifyBankAcc;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class BankAccountsAdapter extends RecyclerView.Adapter<BankAccountsAdapter.BankAccountItemHolder> {

    private BankAccountsAdapter.OnItemClickListener listener;
    private List<CoinifyBankAcc> bankAccsList;

    public BankAccountsAdapter(List<CoinifyBankAcc> bankAccsList) {
        this.bankAccsList = bankAccsList;
    }


    @Override
    public BankAccountItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bank_acc, parent, false);
        return new BankAccountItemHolder(view);
    }

    public void setListener(BankAccountsAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(BankAccountItemHolder holder, final int position) {
        holder.cv_bank_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int id = bankAccsList.get(position).getId();
                    String bankName = bankAccsList.get(position).getBank().getName();
                    String holderName = bankAccsList.get(position).getHolder().getName();
                    String accNumber = bankAccsList.get(position).getAccount().getNumber();
                    listener.OnItemClick(position, id, bankName, holderName, accNumber);
                }
            }
        });

        holder.tv_bank_name.setText(bankAccsList.get(position).getBank().getName());
        holder.tv_holder.setText(bankAccsList.get(position).getHolder().getName());
        holder.tv_currency.setText(bankAccsList.get(position).getAccount().getCurrency());
        holder.tv_number.setText(bankAccsList.get(position).getAccount().getNumber());

    }

    @Override
    public int getItemCount() {
        return bankAccsList.size();
    }

    public interface OnItemClickListener {
        void OnItemClick(int position, int bankAccId, String bankName, String holderName, String accNumber);
    }

    class BankAccountItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.cv_bank_acc)
        CardView cv_bank_acc;
        @BindView(R.id.tv_bank_name)
        TextView tv_bank_name;
        @BindView(R.id.tv_holder)
        TextView tv_holder;
        @BindView(R.id.tv_currency)
        TextView tv_currency;
        @BindView(R.id.tv_number)
        TextView tv_number;

        BankAccountItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
