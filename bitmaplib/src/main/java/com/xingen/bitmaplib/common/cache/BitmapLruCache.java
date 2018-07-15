package com.xingen.bitmaplib.common.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

/**
 * Author by ${xinGen},  Date on 2018/5/28.
 *
 * 内存
 */
public class BitmapLruCache  extends LruCache<String,Bitmap> {
    // Use 1/10th of the available memory for this memory cache.
    private  static final int IMAGE_CACHE_SIZE = ((int) Runtime.getRuntime().maxMemory() / 1024) / 10;

    public BitmapLruCache() {
        super(IMAGE_CACHE_SIZE);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getByteCount() / 1024;
    }
}
