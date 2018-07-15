package com.xingen.bitmaplib.internal;

import android.graphics.Bitmap;

/**
 * Author by ${xinGen},  Date on 2018/5/28.
 */
public interface BitmapListener {
    void result(String imageId, Bitmap bitmap);
    void error(String imageId, Exception e);
}
