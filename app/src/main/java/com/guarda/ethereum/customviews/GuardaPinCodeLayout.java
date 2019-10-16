package com.guarda.ethereum.customviews;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guarda.ethereum.R;

public class GuardaPinCodeLayout extends LinearLayout {

    private boolean isShowComma = true;
    private int pinCodeLeftRight = 24;
    private int pinCodeMarginBottom = 24;
    private Integer maxCount = 4;

    private final int VIBRATE_ERROR_TIME = 200;

    private Context context;
    private LinearLayout pinCodeLayout;
    private GuardaInputLayout guardaInputLayout;

    private OnPinCodeListener listener;
    private TextView warningText;
    private View warningView;

    public interface OnPinCodeListener {
        void onTextChanged(String text);
    }

    public GuardaPinCodeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initXmlStyle(attrs);
        init(context);
    }

    public GuardaPinCodeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initXmlStyle(attrs);
        init(context);
    }

    public GuardaPinCodeLayout(Context context) {
        super(context);
        init(context);
    }

    public void disableButtons() {
        guardaInputLayout.disabeleInput();
    }

    public void setErrorMessage(String msg) {
        warningView.setVisibility(VISIBLE);
        warningText.setText(msg);
    }

    public void removeError() {
        warningView.setVisibility(INVISIBLE);
    }

    public void enableButtons() {
        guardaInputLayout.enableInput();
    }

    public void setInputListener(OnPinCodeListener listener) {
        this.listener = listener;
    }

    private void initXmlStyle(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GuardaPinCodeLayout);
            isShowComma = a.getBoolean(R.styleable.GuardaPinCodeLayout_pinShowComma, isShowComma);
            pinCodeLeftRight = a.getDimensionPixelSize(R.styleable.GuardaPinCodeLayout_pinCodeMarginLeftRight, pinCodeLeftRight);
            pinCodeMarginBottom = a.getDimensionPixelSize(R.styleable.GuardaPinCodeLayout_pinCodeMarginBottom, pinCodeMarginBottom);
            maxCount = a.getInt(R.styleable.GuardaPinCodeLayout_pinCodeMaxCount, maxCount);
        }
    }

    private void init(final Context context) {
        this.context = context;
        setWillNotDraw(false);
        setOrientation(LinearLayout.VERTICAL);

        pinCodeLayout = new LinearLayout(context);
        pinCodeLayout.setId(R.id.pin_header);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        p.bottomMargin = pinCodeMarginBottom;
        p.leftMargin = pinCodeLeftRight;
        p.rightMargin = pinCodeLeftRight;
        pinCodeLayout.setLayoutParams(p);
        pinCodeLayout.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 1; i <= maxCount; i++) {
            pinCodeLayout.addView(getPinImageView(context));
        }

        guardaInputLayout = new GuardaInputLayout(context);

        RelativeLayout.LayoutParams giParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        giParam.addRule(RelativeLayout.BELOW, R.id.pin_header);
        guardaInputLayout.setLayoutParams(giParam);

        guardaInputLayout.isShowComma(isShowComma);
        guardaInputLayout.setMaxCount(maxCount);


        guardaInputLayout.setInputListener((String inputText) -> {
            checkPinCodeImg(inputText.length());

            if (listener != null) {
                listener.onTextChanged(inputText);
            }
        });

        addView(pinCodeLayout);
        addView(getWarningView());
        addView(guardaInputLayout);
    }

    private View getWarningView() {
        warningView = LayoutInflater.from(context).inflate(R.layout.pin_code_warning_layout, null);
        warningText = warningView.findViewById(R.id.tv_warning_text);
        warningView.setVisibility(INVISIBLE);
        return warningView;
    }

    private void checkPinCodeImg(int colCheckedPin) {
        for (int i = 0; i < maxCount; i++) {
            ImageView pinImageView = (ImageView) pinCodeLayout.getChildAt(i);
            if (i < colCheckedPin) {
                pinImageView.setImageResource(R.drawable.ic_pin_code_fill);
            } else {
                pinImageView.setImageResource(R.drawable.ic_pin_code);
            }
        }
        invalidate();
    }

    private View getPinImageView(Context context) {
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.ic_pin_code);

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.weight = 1;
        imageView.setLayoutParams(p);

        return imageView;
    }

    public GuardaInputLayout getInputLayout() {
        return guardaInputLayout;
    }

    public void callError() {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(VIBRATE_ERROR_TIME);
        new Handler().postDelayed(() -> {
            guardaInputLayout.clearText();
            setErrorMessage(getResources().getString(R.string.incorrect_pin_warning));
        }, VIBRATE_ERROR_TIME);
    }

}
