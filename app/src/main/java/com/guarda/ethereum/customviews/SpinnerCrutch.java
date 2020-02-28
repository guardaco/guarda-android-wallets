package com.guarda.ethereum.customviews;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;

public class SpinnerCrutch extends AppCompatSpinner {

    public SpinnerCrutch (Context context) {
        super(context);
    }

    public SpinnerCrutch(Context context, int mode) {
        super(context, mode);
    }

    public SpinnerCrutch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpinnerCrutch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SpinnerCrutch(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
        super(context, attrs, defStyleAttr, mode);
    }

    public SpinnerCrutch(Context context, AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, mode, popupTheme);
    }

    @Override
    public void getWindowVisibleDisplayFrame(Rect outRect) {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        d.getRectSize(outRect);
        super.getWindowVisibleDisplayFrame(outRect);
        outRect.set(outRect.left, getTop(), outRect.right, outRect.bottom - 10);
    }

}
