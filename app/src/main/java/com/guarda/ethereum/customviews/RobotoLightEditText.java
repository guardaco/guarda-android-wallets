package com.guarda.ethereum.customviews;

import android.content.Context;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.AttributeSet;


public class RobotoLightEditText extends AppCompatEditText {
    private OnPasteTextListener mOnPasteListener;
    public RobotoLightEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoLightEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoLightEditText(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");
            setTypeface(tf);
        }
    }

    @Override
    public boolean onTextContextMenuItem(int id) {
        boolean consumed = super.onTextContextMenuItem(id);
        switch (id) {
            case android.R.id.paste:
                notifyOnPasteListener();
                break;
        }
        return consumed;
    }

    public void setOnPasteListener(OnPasteTextListener listener) {
        this.mOnPasteListener = listener;
    }

    private void notifyOnPasteListener() {
        if (mOnPasteListener != null) {
            mOnPasteListener.onPasteText(getText().toString());
        }
    }

    public interface OnPasteTextListener {
        void onPasteText(String text);
    }
}
