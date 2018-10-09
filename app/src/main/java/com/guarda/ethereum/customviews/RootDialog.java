package com.guarda.ethereum.customviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import com.guarda.ethereum.R;

/**
 * Created by psd on 24.01.2018.
 */

public class RootDialog extends android.support.v4.app.DialogFragment {
    public static final String TAG = "RootDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_DeviceDefault_Light_Dialog);
        }
        builder.setMessage(R.string.root_dialog_message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //
            }
        });
        return builder.create();
    }
}
