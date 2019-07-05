package com.guarda.ethereum.customviews;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guarda.ethereum.R;

public class GuardaInputLayout extends GridLayout implements View.OnClickListener {

    private String currentText = "";

    private int textSize = 24;
    private boolean isShowComma = true;
    private Integer maxCount = 10;
    private View commaView;

    private boolean isInputBlocked = false;

    public interface onGuardaInputLayoutListener {
        void onTextChanged(String inputText);
    }

    private onGuardaInputLayoutListener listener;

    public GuardaInputLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initXmlStyle(attrs);
        init(context);
    }

    public GuardaInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initXmlStyle(attrs);
        init(context);
    }

    public GuardaInputLayout(Context context) {
        super(context);
        init(context);
    }

    private void initXmlStyle(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GuardaInputLayout);
            textSize = a.getInt(R.styleable.GuardaInputLayout_inputTextSize, textSize);
            isShowComma = a.getBoolean(R.styleable.GuardaInputLayout_isShowComma, isShowComma);
            maxCount = a.getInteger(R.styleable.GuardaInputLayout_maxCount, maxCount);
        }
    }

    public void setInputListener(onGuardaInputLayoutListener listener) {
        this.listener = listener;
    }

    private void init(Context context) {
        setWillNotDraw(false);

        setColumnCount(3);
        setOrientation(HORIZONTAL);
        setRowCount(4);

        for (int i = 1; i <= 9; i++) {
            TextView view = getTextView(context);
            view.setText(String.valueOf(i));
            Integer id = context.getResources().getIdentifier(("input_" + i), "id", context.getPackageName());
            view.setId(id);
            view.setOnClickListener(this);
            addView(view);
        }

        commaView = getCommaView(context);
        addView(commaView);

        addView(getZeroView(context));
        addView(getEraseView(context));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isShowComma) {
            commaView.setVisibility(VISIBLE);
        } else {
            commaView.setVisibility(GONE);
        }
    }

    private View getEraseView(Context context) {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.setGravity(Gravity.FILL);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);

        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setLayoutParams(params);
        relativeLayout.setGravity(Gravity.CENTER);
        relativeLayout.setBackground(context.getResources().getDrawable(R.drawable.ripple));
        relativeLayout.setId(R.id.input_erase);

        ImageView eraseView = new ImageView(context);
        eraseView.setImageResource(R.drawable.ic_erase);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        eraseView.setLayoutParams(layoutParams);

        relativeLayout.addView(eraseView);
        relativeLayout.setOnClickListener(this);
        return relativeLayout;
    }

    private View getZeroView(Context context) {
        TextView viewZero = getTextView(context);

        viewZero.setText("0");
        viewZero.setId(R.id.input_0);
        viewZero.setOnClickListener(this);
        return viewZero;
    }

    private View getCommaView(Context context) {
        if (isShowComma) {
            TextView viewComma = getTextView(context);
            viewComma.setText(".");
            viewComma.setId(R.id.input_comma);
            viewComma.setOnClickListener(this);
            return viewComma;
        } else {
            return getEmptyTextView(context);
        }

    }

    private TextView getTextView(Context context) {
        RobotoLightTextView textView = new RobotoLightTextView(context);
        textView.setBackground(context.getResources().getDrawable(R.drawable.ripple));
        textView.setClickable(true);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(context.getResources().getColor(R.color.inputKeyboardTextColor));
        textView.setTextSize(textSize);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.setGravity(Gravity.FILL);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);

        textView.setLayoutParams(params);

        return textView;
    }

    private TextView getEmptyTextView(Context context) {
        TextView textView = new TextView(context);
        textView.setClickable(false);
        textView.setGravity(Gravity.CENTER);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.setGravity(Gravity.FILL);
        params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);

        textView.setLayoutParams(params);

        return textView;
    }

    @Override
    public void onClick(View v) {
        if (!isInputBlocked) {
            switch (v.getId()) {
                case R.id.input_1:
                    addSymbolToAmount("1");
                    break;
                case R.id.input_2:
                    addSymbolToAmount("2");
                    break;
                case R.id.input_3:
                    addSymbolToAmount("3");
                    break;
                case R.id.input_4:
                    addSymbolToAmount("4");
                    break;
                case R.id.input_5:
                    addSymbolToAmount("5");
                    break;
                case R.id.input_6:
                    addSymbolToAmount("6");
                    break;
                case R.id.input_7:
                    addSymbolToAmount("7");
                    break;
                case R.id.input_8:
                    addSymbolToAmount("8");
                    break;
                case R.id.input_9:
                    addSymbolToAmount("9");
                    break;
                case R.id.input_comma:
                    if (currentText.isEmpty())
                        addSymbolToAmount("0.");
                    else if (!currentText.isEmpty() && !currentText.contains(".") && currentText.charAt(0) != '.')
                        addSymbolToAmount(".");

                    break;
                case R.id.input_0:
                    addSymbolToAmount("0");
                    break;
                case R.id.input_erase:
                    eraseSymbol();
                    break;
            }
        }
    }

    private void addSymbolToAmount(String symbol) {
        String oldText = currentText.isEmpty() ? "" : currentText;
        if (maxCount == null) {
            currentText = oldText + symbol;
        } else {
            if (maxCount > currentText.length()){
                currentText = oldText + symbol;
            }
        }
        if (listener != null) {
            listener.onTextChanged(currentText);
        }
    }

    private void eraseSymbol() {
        if (!currentText.isEmpty()) {
            currentText = currentText.substring(0, currentText.length() - 1);
            if (listener != null) {
                listener.onTextChanged(currentText);
            }
        }
    }

    public void isShowComma(boolean isShowComma) {
        this.isShowComma = isShowComma;
        this.postInvalidate();
    }

    public void setMaxCount(Integer count) {
        this.maxCount = count;
    }

    public void clearText(){
        if (currentText != null){
            currentText = "";
            if (listener != null) {
                listener.onTextChanged(currentText);
            }
        }
    }

    public void enableInput(){
        isInputBlocked = false;
    }

    public void disabeleInput(){
        isInputBlocked = true;
    }

    public void setCurrentText(String currentText) {
        this.currentText = currentText;
    }
}
