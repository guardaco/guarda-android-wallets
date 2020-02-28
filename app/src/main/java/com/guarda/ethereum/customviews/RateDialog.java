package com.guarda.ethereum.customviews;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.freshchat.consumer.sdk.Freshchat;
import com.guarda.ethereum.GuardaApp;
import com.guarda.ethereum.R;
import com.guarda.ethereum.managers.SharedManager;

import javax.inject.Inject;

import autodagger.AutoInjector;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by psd on 06.12.2017.
 */

@AutoInjector(GuardaApp.class)
public class RateDialog extends DialogFragment {

    @BindView(R.id.const_rate)
    ConstraintLayout const_rate;

    @BindView(R.id.frame_chat_mail)
    ConstraintLayout frame_chat_mail;

    @BindView(R.id.rate_star_1)
    ImageView rate_star_1;

    @BindView(R.id.rate_star_2)
    ImageView rate_star_2;

    @BindView(R.id.rate_star_3)
    ImageView rate_star_3;

    @BindView(R.id.rate_star_4)
    ImageView rate_star_4;

    @BindView(R.id.rate_star_5)
    ImageView rate_star_5;

    @BindView(R.id.rate_star_fill_1)
    ImageView rate_star_fill_1;

    @BindView(R.id.rate_star_fill_2)
    ImageView rate_star_fill_2;

    @BindView(R.id.rate_star_fill_3)
    ImageView rate_star_fill_3;

    @BindView(R.id.rate_star_fill_4)
    ImageView rate_star_fill_4;

    @BindView(R.id.rate_star_fill_5)
    ImageView rate_star_fill_5;

    @BindView(R.id.button_rate_later)
    Button button_rate_later;

    @BindView(R.id.button_rate_ok)
    Button button_rate_ok;

    @BindView(R.id.send_to_chat)
    Button send_to_chat;

    @BindView(R.id.rate_cancel)
    Button rate_cancel;

    private int rate;

    @Inject
    Context context;

    @Inject
    SharedManager sharedManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_rate, container, false);
        ButterKnife.bind(this, view);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(false);
        return view;
    }

    public RateDialog() {
        super();
        GuardaApp.getAppComponent().inject(this);
    }

    @OnClick(R.id.button_rate_later)
    public void later(){
        dismiss();
    }

    @OnClick(R.id.button_rate_ok)
    public void rate(){
        if (rate < 1) {
            Toast.makeText(context, getString(R.string.rate_no_stars), Toast.LENGTH_SHORT).show();
        } else if (rate <= 3) {
            const_rate.setVisibility(View.INVISIBLE);
            frame_chat_mail.setVisibility(View.VISIBLE);
        } else {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            } catch (Exception e){
                Log.e("psd", "cant open play market " + e.toString());
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
            dismiss();
        }

        sharedManager.setIsAskRate(false);
    }

    @OnClick({R.id.rate_star_1, R.id.rate_star_2, R.id.rate_star_3, R.id.rate_star_4, R.id.rate_star_5})
    public void rateStar(View view){
        rate_star_fill_1.setVisibility(View.INVISIBLE);
        rate_star_fill_2.setVisibility(View.INVISIBLE);
        rate_star_fill_3.setVisibility(View.INVISIBLE);
        rate_star_fill_4.setVisibility(View.INVISIBLE);
        rate_star_fill_5.setVisibility(View.INVISIBLE);
        switch (view.getId()){
            case R.id.rate_star_1:
                rate = 1;
                rate_star_fill_1.setVisibility(View.VISIBLE);
                break;
            case R.id.rate_star_2:
                rate = 2;
                rate_star_fill_1.setVisibility(View.VISIBLE);
                rate_star_fill_2.setVisibility(View.VISIBLE);
                break;
            case R.id.rate_star_3:
                rate = 3;
                rate_star_fill_1.setVisibility(View.VISIBLE);
                rate_star_fill_2.setVisibility(View.VISIBLE);
                rate_star_fill_3.setVisibility(View.VISIBLE);
                break;
            case R.id.rate_star_4:
                rate = 4;
                rate_star_fill_1.setVisibility(View.VISIBLE);
                rate_star_fill_2.setVisibility(View.VISIBLE);
                rate_star_fill_3.setVisibility(View.VISIBLE);
                rate_star_fill_4.setVisibility(View.VISIBLE);
                break;
            case R.id.rate_star_5:
                rate = 5;
                rate_star_fill_1.setVisibility(View.VISIBLE);
                rate_star_fill_2.setVisibility(View.VISIBLE);
                rate_star_fill_3.setVisibility(View.VISIBLE);
                rate_star_fill_4.setVisibility(View.VISIBLE);
                rate_star_fill_5.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnClick(R.id.send_to_chat)
    public void openChat(){
        dismiss();
        Freshchat.showConversations(context);
    }

    @OnClick(R.id.rate_cancel)
    public void cancel(){
        dismiss();
    }
}
