package com.guarda.ethereum.utils;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.Map;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;

public final class QrCodeUtils {

    private QrCodeUtils() {
    }

    public static Bitmap textToQrCode(String Value, int width) {
        Map<EncodeHintType, Object> hintMap = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        // Now with zxing version 3.2.1 you could change border size (white border size to just 1)
        hintMap.put(EncodeHintType.MARGIN, 0); /* default = 4 */
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    width, width, hintMap
            );

        } catch (IllegalArgumentException ignored) {

        } catch (WriterException e) {
            e.printStackTrace();
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, width, 0, 0, bitMatrixWidth, bitMatrixHeight);

        return bitmap;
    }

    public static Bitmap textToBarCode(String data) {
        MultiFormatWriter writer = new MultiFormatWriter();


        String finaldata = Uri.encode(data, "utf-8");

        BitMatrix bm = null;
        try {
            bm = writer.encode(finaldata, BarcodeFormat.CODE_128, 150, 150);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = Bitmap.createBitmap(180, 40, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < 180; i++) {//width
            for (int j = 0; j < 40; j++) {//height
                bitmap.setPixel(i, j, bm.get(i, j) ? BLACK : WHITE);
            }
        }

        return bitmap;
    }
}
