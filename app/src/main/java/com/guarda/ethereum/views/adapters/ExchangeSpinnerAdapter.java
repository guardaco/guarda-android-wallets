package com.guarda.ethereum.views.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.guarda.ethereum.R;
import com.guarda.ethereum.models.ExchangeSpinnerRowModel;
import com.squareup.picasso.Picasso;

import java.util.List;

import timber.log.Timber;


public class ExchangeSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

    private LayoutInflater inflater;
    private List<ExchangeSpinnerRowModel> rows;

    public ExchangeSpinnerAdapter(Context applicationContext, List<ExchangeSpinnerRowModel> rows) {
        this.rows = rows;
        this.inflater = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Object getItem(int i) {
        try {
            return rows.get(i);
        } catch (IndexOutOfBoundsException iobe) {
            iobe.printStackTrace();
            return rows.get(0);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.spinner_item, null);

        try {
            AppCompatImageView icon = view.findViewById(R.id.imageView);

            Drawable coinIcon = inflater.getContext().getResources().getDrawable(R.drawable.ic_icon_image_shapeshift);
            Integer id = inflater.getContext().getResources().getIdentifier("ic_" + rows.get(i).symbol.toLowerCase(), "drawable", inflater.getContext().getPackageName());
            if (id != null && id != 0) {
                coinIcon = inflater.getContext().getResources().getDrawable(id);
            } else {
                if (rows.size() == 1 && rows.get(0).text.equalsIgnoreCase("changenow")) {
                    coinIcon = inflater.getContext().getResources().getDrawable(R.drawable.ic_change_now);
                } else {
                    coinIcon = inflater.getContext().getResources().getDrawable(R.drawable.ic_curr_empty);
                }

            }
            icon.setImageDrawable(coinIcon);

            TextView name = (TextView) view.findViewById(R.id.textView);
            name.setText(rows.get(i).text);
        } catch (Exception iobe) {
            iobe.printStackTrace();
            Timber.e("adapter getView e=%s", iobe.getMessage());
        }

        return view;
    }

    public void removeItemBySymbol(String symbol) {
        for (int i = 0; i < rows.size(); ++i) {
            ExchangeSpinnerRowModel row = rows.get(i);
            if (row.symbol.equals(symbol)) {
                rows.remove(i);
                break;
            }
        }
    }

}
