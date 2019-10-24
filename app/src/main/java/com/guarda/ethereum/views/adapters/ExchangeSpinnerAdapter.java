package com.guarda.ethereum.views.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.guarda.ethereum.R;
import com.guarda.ethereum.models.ExchangeSpinnerRowModel;
import com.guarda.ethereum.utils.svg.GlideApp;
import com.guarda.ethereum.utils.svg.SvgSoftwareLayerSetter;

import java.util.List;

import timber.log.Timber;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


public class ExchangeSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

    private LayoutInflater inflater;
    private List<ExchangeSpinnerRowModel> rows;
    private RequestBuilder<PictureDrawable> requestBuilder;

    public ExchangeSpinnerAdapter(Context applicationContext, List<ExchangeSpinnerRowModel> rows) {
        this.rows = rows;
        this.inflater = (LayoutInflater.from(applicationContext));

        requestBuilder = GlideApp.with(applicationContext)
                .as(PictureDrawable.class)
                .transition(withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .error(R.drawable.ic_curr_empty_black)
                .listener(new SvgSoftwareLayerSetter());
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    public void updateRows(List<ExchangeSpinnerRowModel> rows) {
        this.rows = rows;
        notifyDataSetChanged();
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
            Drawable coinIcon;
            ExchangeSpinnerRowModel item = rows.get(i);

            String cnTicker = item.symbol;
            Integer id = inflater.getContext().getResources().getIdentifier("ic_" + cnTicker.toLowerCase(), "drawable", inflater.getContext().getPackageName());
            if (id != null && id != 0) {
                coinIcon = inflater.getContext().getResources().getDrawable(id);
                coinIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                icon.setImageDrawable(coinIcon);
            } else {
                //only for first spinner with services list (changenow only now)
                if (rows.size() == 1 && rows.get(0).text.equalsIgnoreCase("changenow")) {
                    coinIcon = inflater.getContext().getResources().getDrawable(R.drawable.ic_change_now);
                    icon.setImageDrawable(coinIcon);
                } else {
                    requestBuilder
                            .load(item.url)
                            .into(icon);
                }
            }

            TextView name = view.findViewById(R.id.textView);
            String cnName = item.text;
            name.setText(cnName);
            if (cnName.isEmpty()) name.setText(cnTicker.toUpperCase());
        } catch (Exception iobe) {
            iobe.printStackTrace();
            Timber.e("adapter getView e=%s", iobe.getMessage());
        }

        return view;
    }

}
