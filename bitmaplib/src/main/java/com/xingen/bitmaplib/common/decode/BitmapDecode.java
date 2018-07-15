package com.xingen.bitmaplib.common.decode;

import android.graphics.Bitmap;

import java.io.IOException;

/**
 * Author by ${xinGen},  Date on 2018/5/28.
 */
public interface BitmapDecode {

    Bitmap decode(String url, int targetWidth, int targetHeight) throws IOException;
}
