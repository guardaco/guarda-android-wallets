package com.guarda.ethereum.utils;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * Created by SV on 29.08.2017.
 */

public class DigitsInputFilter implements InputFilter {

    private final String DOT = ".";

    private int maxIntegerDigitsLength;
    private int maxDigitsAfterLength;
    private double max;


    public DigitsInputFilter(int maxDigitsBeforeDot, int maxDigitsAfterDot, double maxValue) {
        maxIntegerDigitsLength = maxDigitsBeforeDot;
        maxDigitsAfterLength = maxDigitsAfterDot;
        max = maxValue;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String allText = getAllText(source, dest, dstart);
        String onlyDigitsText = getOnlyDigitsPart(allText);

        if (allText.isEmpty()) {
            return null;
        } else {
            double enteredValue;
            try {
                enteredValue = Double.parseDouble(onlyDigitsText);
            } catch (NumberFormatException e) {
                return "";
            }
            return checkMaxValueRule(enteredValue, onlyDigitsText);
        }
    }


    private CharSequence checkMaxValueRule(double enteredValue, String onlyDigitsText) {
        if (enteredValue > max) {
            return "";
        } else {
            return handleInputRules(onlyDigitsText);
        }
    }

    private CharSequence handleInputRules(String onlyDigitsText) {
        if (isDecimalDigit(onlyDigitsText)) {
            return checkRuleForDecimalDigits(onlyDigitsText);
        } else {
            return checkRuleForIntegerDigits(onlyDigitsText.length());
        }
    }

    private boolean isDecimalDigit(String onlyDigitsText) {
        return onlyDigitsText.contains(DOT);
    }

    private CharSequence checkRuleForDecimalDigits(String onlyDigitsPart) {
        String afterDotPart = onlyDigitsPart.substring(onlyDigitsPart.indexOf(DOT), onlyDigitsPart.length() - 1);
        if (afterDotPart.length() > maxDigitsAfterLength) {
            return "";
        }
        return null;
    }

    private CharSequence checkRuleForIntegerDigits(int allTextLength) {
        if (allTextLength > maxIntegerDigitsLength) {
            return "";
        }
        return null;
    }

    private String getOnlyDigitsPart(String text) {
        return text.replaceAll("[^0-9?!\\.]", "");
    }

    private String getAllText(CharSequence source, Spanned dest, int dstart) {
        String allText = "";
        if (!dest.toString().isEmpty()) {
            if (source.toString().isEmpty()) {
                allText = deleteCharAtIndex(dest, dstart);
            } else {
                allText = new StringBuilder(dest).insert(dstart, source).toString();
            }
        }
        return allText;
    }

    private String deleteCharAtIndex(Spanned dest, int dstart) {
        StringBuilder builder = new StringBuilder(dest);
        if (dest.length() > 0) {
            builder.deleteCharAt(dstart);
        }
        return builder.toString();
    }
}