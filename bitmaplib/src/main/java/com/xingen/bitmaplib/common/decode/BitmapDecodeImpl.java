package com.xingen.bitmaplib.common.decode;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.xingen.bitmaplib.common.cache.CacheClient;
import com.xingen.bitmaplib.common.constants.Constants;
import com.xingen.bitmaplib.common.net.NetUtils;
import com.xingen.bitmaplib.common.scale.BitmapScaleUtils;
import com.xingen.bitmaplib.common.utils.MD5Utils;
import com.xingen.bitmaplib.internal.BitmapRequest;

import java.io.IOException;
import java.io.InputStream;

/**
 * Author by ${xinGen},  Date on 2018/5/28.
 * <p>
 * 解码生成Bitmap
 */
public class BitmapDecodeImpl implements BitmapDecode {
    /**
     * 一个同步锁，防止同一时刻，产生大量内存
     */
    private static final Object lock = new Object();
    private Resources resources;
    private AssetManager assetManager;
    private CacheClient cacheClient;

    public BitmapDecodeImpl(Resources resources, AssetManager assetManager, CacheClient cacheClient) {
        this.resources = resources;
        this.assetManager = assetManager;
        this.cacheClient = cacheClient;
    }

    @Override
    public Bitmap decode(String url, int targetWidth, int targetHeight) throws IOException {
        String key = BitmapRequest.CacheKeyUtils.createKey(url, targetWidth, targetHeight);
        Bitmap bitmap = cacheClient.fromLruCache(key);
        if (bitmap == null) {
            if (url.contains(Constants.PathPrefix.Prefix_Http)) {
                bitmap = decodeFromNet(url, MD5Utils.hashImageUrlForDisk(url), targetWidth, targetHeight);
            } else if (url.contains(Constants.PathPrefix.Prefix_Drawable) || url.contains(Constants.PathPrefix.Prefix_Mipmap)) {
                int actualImageId = Integer.valueOf(Constants.PathPrefix.getActualImageId(url));
                bitmap = decodeFromResource(actualImageId, targetWidth, targetHeight);
            } else if (url.contains(Constants.PathPrefix.Prefix_Asset)) {
                String actualImageId = Constants.PathPrefix.getActualImageId(url);
                bitmap = decodeFromAsset(actualImageId, targetWidth, targetHeight);
            } else if (url.contains(Constants.PathPrefix.Prefix_File)) {
                String actualImageId = Constants.PathPrefix.getActualImageId(url);
                bitmap = decodeFromFile(actualImageId, targetWidth, targetHeight);
            }
            cacheClient.toLruCache(key, bitmap);
        }
        return bitmap;
    }

    /**
     * 从Resource下Drawable或Mipmap文件夹下解析bitmap
     *
     * @param imageId
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    private Bitmap decodeFromResource(int imageId, int targetWidth, int targetHeight) throws NullPointerException, IOException {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, imageId, options);
        final int actualHeight = options.outHeight;
        final int actualWidth = options.outWidth;
        options.inSampleSize = BitmapScaleUtils.calculateBitmapScaleValue(targetWidth, targetHeight, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        synchronized (lock) {
            bitmap = BitmapFactory.decodeResource(resources, imageId, options);
        }
        return bitmap;
    }

    /**
     * 从Asset文件夹下生成Bitmap
     *
     * @param imageId
     * @param targetWidth
     * @param targetHeight
     * @return
     * @throws IOException
     */
    private Bitmap decodeFromAsset(String imageId, int targetWidth, int targetHeight) throws NullPointerException, IOException {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream inputStream = assetManager.open(imageId);
        Rect rect = new Rect();
        BitmapFactory.decodeStream(inputStream, rect, options);
        if (inputStream != null) {
            inputStream.close();
        }
        final int actualHeight = options.outHeight;
        final int actualWidth = options.outWidth;
        options.inSampleSize = BitmapScaleUtils.calculateBitmapScaleValue(targetWidth, targetHeight, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        inputStream = assetManager.open(imageId);
        synchronized (lock) {
            bitmap = BitmapFactory.decodeStream(inputStream, rect, options);
        }
        if (inputStream != null) {
            inputStream.close();
        }
        return bitmap;
    }

    /**
     * 从File文件中生成Bitmap
     *
     * @param imageId
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    private Bitmap decodeFromFile(String imageId, int targetWidth, int targetHeight) throws NullPointerException, IOException {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageId, options);
        final int actualHeight = options.outHeight;
        final int actualWidth = options.outWidth;
        options.inSampleSize = BitmapScaleUtils.calculateBitmapScaleValue(targetWidth, targetHeight, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        synchronized (lock) {
            bitmap = BitmapFactory.decodeFile(imageId, options);
        }
        return bitmap;
    }

    /**
     * 从NetWork中生成Bitmap
     *
     * @param key
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    private Bitmap decodeFromNet(String url, String key, int targetWidth, int targetHeight) throws IOException {
        Bitmap bitmap = null;
        NetUtils.CacheHeader cacheHeader = NetUtils.HttpCacheUtils.getCacheHeader(key);
        if (cacheHeader != null) {
            if (!cacheHeader.isExpired()) {
                synchronized (lock){
                    bitmap = cacheClient.fromDiskLruCache(key, targetWidth, targetHeight);
                }
            } else {//过期了
                NetUtils.executeRequest(url, key, cacheClient, true);
                synchronized (lock) {
                    bitmap = cacheClient.fromDiskLruCache(key, targetWidth, targetHeight);
                }
            }
        } else {//没有缓存
            NetUtils.executeRequest(url, key, cacheClient, false);
            synchronized (lock) {
                bitmap = cacheClient.fromDiskLruCache(key, targetWidth, targetHeight);
            }
        }

        return bitmap;
    }

}
