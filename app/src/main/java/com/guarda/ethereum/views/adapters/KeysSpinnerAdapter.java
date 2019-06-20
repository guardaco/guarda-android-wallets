package com.guarda.ethereum.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.guarda.ethereum.R;
import com.guarda.ethereum.models.KeysSpinnerRowModel;

import java.util.List;


public class KeysSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

    private LayoutInflater inflater;
    private List<KeysSpinnerRowModel> rows;

    public KeysSpinnerAdapter(Context applicationContext, List<KeysSpinnerRowModel> rows) {
        this.inflater = (LayoutInflater.from(applicationContext));
        this.rows = rows;
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Object getItem(int i) {
        return rows.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.spinner_restore_item, null);

        try {
            TextView keyTitle = view.findViewById(R.id.tvRestoreItem);
            keyTitle.setText(rows.get(i).title);
        } catch (IndexOutOfBoundsException iobe) {
            iobe.printStackTrace();
        }

        return view;
    }

}
