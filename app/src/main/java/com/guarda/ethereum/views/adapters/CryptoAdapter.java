package com.guarda.ethereum.views.adapters;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.guarda.ethereum.R;
import com.guarda.ethereum.models.items.CryptoItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.guarda.ethereum.models.constants.Common.MAIN_CURRENCY;

public class CryptoAdapter extends RecyclerView.Adapter<CryptoItemHolder> {

    public interface OnItemClickListener {
        void OnItemClick(int position, String name, String code);
    }

    OnItemClickListener listener;

    private List<CryptoItem> cryptoList;
    private View view;

    public CryptoAdapter(List<CryptoItem> transList) {
        this.cryptoList = transList;
    }

    public CryptoAdapter() {
        this.cryptoList = new ArrayList<>();
    }

    public void setItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateList(List<CryptoItem> transList) {
        this.cryptoList = transList;
        filterList();
        notifyDataSetChanged();
    }

    private void filterList() {
        for (int i = 0; i < cryptoList.size(); i++){
            if (cryptoList.get(i).getCode().equals(MAIN_CURRENCY.toUpperCase())){
                cryptoList.remove(i);
            }
        }
    }

    @Override
    public CryptoItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_purchase_crypto, parent, false);
        return new CryptoItemHolder(view);
    }

    @Override
    public void onBindViewHolder(CryptoItemHolder holder, final int position) {
        final CryptoItem item = cryptoList.get(position);

        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.OnItemClick(position, item.getName(), item.getCode());
                }
            }
        });

        holder.tvName.setText(item.getName());

        Integer id = view.getContext().getResources().getIdentifier("ic_" + item.getCode().toLowerCase(), "drawable", view.getContext().getPackageName());
    }

    @Override
    public int getItemCount() {
        return cryptoList.size();
    }

}


class CryptoItemHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.main_view)
    View rootView;
    @BindView(R.id.iv_crypto_icon)
    ImageView ivIcon;
    @BindView(R.id.tv_crypto_name)
    TextView tvName;


    CryptoItemHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
