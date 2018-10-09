package com.guarda.ethereum.views.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.R;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.ethereum.views.fragments.EnterEmailCoinifyFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PurchaseServicesAdapter extends RecyclerView.Adapter<PurchaseServicesAdapter.PurchaseServicesItemHolder> {

    private PurchaseServicesAdapter.OnItemClickListener buyListener;
    private PurchaseServicesAdapter.OnItemClickListener sellListener;

    private Context context;

    public PurchaseServicesAdapter(Context context) {
        this.context = context;
    }


    @Override
    public PurchaseServicesItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_purchase_services, parent, false);
        return new PurchaseServicesItemHolder(view);
    }

    public void setBuyClickListener(PurchaseServicesAdapter.OnItemClickListener listener) {
        this.buyListener = listener;
    }

    public void setSellClickListener(PurchaseServicesAdapter.OnItemClickListener listener) {
        this.sellListener = listener;
    }

    @Override
    public void onBindViewHolder(PurchaseServicesItemHolder holder, final int position) {
        holder.buttonBuyPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buyListener != null) {
                    buyListener.OnItemClick(position);
                }
            }
        });

        holder.buttonSellPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sellListener != null) {
                    sellListener.OnItemClick(position);
                }
            }
        });

        switch (position) {
            case 0:
                if (Common.MAIN_CURRENCY.equalsIgnoreCase("bts") ||
                        Common.MAIN_CURRENCY.equalsIgnoreCase("zec") ||
                        Common.MAIN_CURRENCY.equalsIgnoreCase("qtum")) {
                    setIndacoinItem(holder);
                } else {
                    setCoinifyItem(holder);
                }
                break;
            case 1:
                if (Common.MAIN_CURRENCY.equalsIgnoreCase("bts") ||
                        Common.MAIN_CURRENCY.equalsIgnoreCase("zec") ||
                        Common.MAIN_CURRENCY.equalsIgnoreCase("qtum")) {
                    //nothing
                } else {
                    setIndacoinItem(holder);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (Common.MAIN_CURRENCY.equalsIgnoreCase("bts") ||
                Common.MAIN_CURRENCY.equalsIgnoreCase("zec") ||
                Common.MAIN_CURRENCY.equalsIgnoreCase("qtum")) {
            return 1;
        } else {
            return 2;
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(int position);
    }

    private void setCoinifyItem(PurchaseServicesItemHolder holder) {
        holder.image_service.setImageResource(R.drawable.ic_coinify);
        holder.tv_service.setText("Coinify");
        if (BuildConfig.DEBUG) {
            holder.rl_sell.setVisibility(View.VISIBLE);
            holder.image_sell_card.setVisibility(View.GONE);
            holder.buttonSellPurchase.setTextColor(context.getResources().getColor(R.color.baseBlueTextColor));
        } else {
            if (!Common.MAIN_CURRENCY.equalsIgnoreCase("btc")) {
                holder.image_sell_bank.setVisibility(View.GONE);
                holder.tv_unavailable.setVisibility(View.VISIBLE);
                holder.buttonSellPurchase.setEnabled(false);
                holder.buttonSellPurchase.setTextColor(context.getResources().getColor(R.color.lightGrey));
            }
            holder.image_sell_card.setVisibility(View.GONE);
        }
        holder.tv_fee.setText("0.25-3%");
        holder.tv_cntr.setText("EU only");
    }

    private void setIndacoinItem(PurchaseServicesItemHolder holder) {
        holder.image_service.setImageResource(R.drawable.ic_indacoin);
        holder.tv_service.setText("Indacoin");
        holder.image_buy_bank.setVisibility(View.INVISIBLE);
        holder.image_sell_card.setVisibility(View.GONE);
        holder.image_sell_bank.setVisibility(View.GONE);
        holder.tv_unavailable.setVisibility(View.VISIBLE);
        holder.tv_fee.setText("~25%");
        holder.tv_cntr.setText("All, except USA");
        holder.buttonSellPurchase.setEnabled(false);
        holder.buttonSellPurchase.setTextColor(context.getResources().getColor(R.color.lightGrey));
    }

    private void setWmcItem(PurchaseServicesItemHolder holder) {
        holder.image_service.setImageResource(R.drawable.ic_wemovecoins);
        holder.tv_service.setText("Wemovecoins");
        holder.image_buy_bank.setVisibility(View.INVISIBLE);
        holder.image_sell_card.setVisibility(View.GONE);
        holder.image_sell_bank.setVisibility(View.GONE);
        holder.tv_unavailable.setVisibility(View.VISIBLE);
        holder.tv_cntr.setText("All, except USA");
        holder.buttonSellPurchase.setEnabled(false);
        holder.tv_fee.setText("2.9-4.2%");
        holder.buttonSellPurchase.setTextColor(context.getResources().getColor(R.color.lightGrey));
    }

    class PurchaseServicesItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.root_item)
        CardView root_item;
        @BindView(R.id.image_service)
        ImageView image_service;
        @BindView(R.id.tv_service)
        TextView tv_service;
        @BindView(R.id.image_buy_card)
        ImageView image_buy_card;
        @BindView(R.id.image_buy_bank)
        ImageView image_buy_bank;
        @BindView(R.id.rl_sell)
        RelativeLayout rl_sell;
        @BindView(R.id.image_sell_card)
        ImageView image_sell_card;
        @BindView(R.id.image_sell_bank)
        ImageView image_sell_bank;
        @BindView(R.id.tv_unavailable)
        TextView tv_unavailable;
        @BindView(R.id.tv_fee)
        TextView tv_fee;
        @BindView(R.id.tv_cntr)
        TextView tv_cntr;
        @BindView(R.id.buttonBuyPurchase)
        Button buttonBuyPurchase;
        @BindView(R.id.buttonSellPurchase)
        Button buttonSellPurchase;

        PurchaseServicesItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
