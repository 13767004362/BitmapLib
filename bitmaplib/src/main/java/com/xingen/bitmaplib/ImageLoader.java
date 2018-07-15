package com.xingen.bitmaplib;

import android.content.Context;
import android.widget.ImageView;

import com.xingen.bitmaplib.common.cache.CacheClient;
import com.xingen.bitmaplib.common.constants.Constants;
import com.xingen.bitmaplib.common.decode.BitmapDecode;
import com.xingen.bitmaplib.common.decode.BitmapDecodeImpl;
import com.xingen.bitmaplib.extral.BitmapContainer;
import com.xingen.bitmaplib.extral.ConcurrentLoader;
import com.xingen.bitmaplib.extral.ScrollImageView;
import com.xingen.bitmaplib.internal.BitmapListener;
import com.xingen.bitmaplib.internal.BitmapRequest;
import com.xingen.bitmaplib.internal.BitmapRequestQueue;


/**
 * Author by ${xinGen},  Date on 2018/5/28.
 */
public class ImageLoader {
    private BitmapRequestQueue requestQueue;
    private static ImageLoader instance;
    private CacheClient cacheClient;
    private ConcurrentLoader concurrentLoader;

    static {
        instance = new ImageLoader();
    }
    private ImageLoader() {

    }
    public static ImageLoader getInstance() {
        return instance;
    }
    public void init(Context context) {
        if (requestQueue == null) {
            this.cacheClient = new CacheClient(context);
            BitmapDecode decode = new BitmapDecodeImpl(context.getResources(), context.getAssets(), cacheClient);
            this.requestQueue = new BitmapRequestQueue(decode, cacheClient);
            this.concurrentLoader = new ConcurrentLoader(requestQueue, cacheClient);
        }
    }
    public BitmapRequest loadNetImage(String url, int targetWidth, int targetHeight, BitmapListener listener) {
        if (requestQueue != null) {
            BitmapRequest request = new BitmapRequest(url, targetWidth, targetHeight, listener);
            this.requestQueue.addRequest(request);
            return request;
        }
        return null;
    }
    public BitmapContainer loadNetImage(ImageView imageView, String url, int targetWidth, int targetHeight, int defaultImageResId, int errorImageResId) {
        return concurrentLoader.loadImage(url, targetWidth, targetHeight,
                BitmapContainer.createImageListener(imageView, defaultImageResId, errorImageResId));
    }
    public void loadNetImage(ScrollImageView scrollImageView, String url,  int defaultImageResId, int errorImageResId) {
        scrollImageView.setDefaultImageId(defaultImageResId);
        scrollImageView.setErrorImageId(errorImageResId);
        scrollImageView.startImageId(url,concurrentLoader);
    }
    public BitmapRequest loadFileImage(String filePath, int targetWidth, int targetHeight, BitmapListener listener) {
        if (requestQueue != null) {
            BitmapRequest request = new BitmapRequest(Constants.ImageIdBuilder.createFile(filePath), targetWidth, targetHeight, listener);
            this.requestQueue.addRequest(request);
            return request;
        }
        return null;
    }
    public void loadFileImage(ScrollImageView scrollImageView, String filePath,  int defaultImageResId, int errorImageResId) {
        scrollImageView.setDefaultImageId(defaultImageResId);
        scrollImageView.setErrorImageId(errorImageResId);
        scrollImageView.startImageId(Constants.ImageIdBuilder.createFile(filePath),concurrentLoader);
    }
    public BitmapContainer loadFileImage(ImageView imageView, String filePath, int targetWidth, int targetHeight, int defaultImageResId, int errorImageResId) {
        return concurrentLoader.loadImage(Constants.ImageIdBuilder.createFile(filePath), targetWidth, targetHeight,
                BitmapContainer.createImageListener(imageView, defaultImageResId, errorImageResId));
    }

    public BitmapRequest loadResourceImage(int imageId, int targetWidth, int targetHeight, BitmapListener listener) {
        if (requestQueue != null) {
            BitmapRequest request = new BitmapRequest(Constants.ImageIdBuilder.createDrawable(imageId), targetWidth, targetHeight, listener);
            this.requestQueue.addRequest(request);
            return request;
        }
        return null;
    }

    public BitmapContainer loadResourceImage(ImageView imageView, int imageId, int targetWidth, int targetHeight, int defaultImageResId, int errorImageResId) {
        return concurrentLoader.loadImage(Constants.ImageIdBuilder.createDrawable(imageId), targetWidth, targetHeight,
                BitmapContainer.createImageListener(imageView, defaultImageResId, errorImageResId));
    }
    public void loadResourceImage(ScrollImageView scrollImageView, int imageId,  int defaultImageResId, int errorImageResId) {
        scrollImageView.setDefaultImageId(defaultImageResId);
        scrollImageView.setErrorImageId(errorImageResId);
        scrollImageView.startImageId(Constants.ImageIdBuilder.createDrawable(imageId),concurrentLoader);
    }
    public BitmapRequest loadAssertImage(String fileName, int targetWidth, int targetHeight, BitmapListener listener) {
        if (requestQueue != null) {
            BitmapRequest request = new BitmapRequest(Constants.ImageIdBuilder.createAsset(fileName), targetWidth, targetHeight, listener);
            this.requestQueue.addRequest(request);
            return request;
        }
        return null;
    }

    public BitmapContainer loadAssertImage(ImageView imageView, String fileName, int targetWidth, int targetHeight, int defaultImageResId, int errorImageResId) {
        return concurrentLoader.loadImage(Constants.ImageIdBuilder.createAsset(fileName), targetWidth, targetHeight,
                BitmapContainer.createImageListener(imageView, defaultImageResId, errorImageResId));
    }
    public void loadAssertImage(ScrollImageView scrollImageView, String fileName,  int defaultImageResId, int errorImageResId) {
           scrollImageView.setDefaultImageId(defaultImageResId);
           scrollImageView.setErrorImageId(errorImageResId);
           scrollImageView.startImageId(Constants.ImageIdBuilder.createAsset(fileName),concurrentLoader);
    }

    public void cancelAll(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }
    public void stop() {
        if (requestQueue != null) {
            requestQueue.stopThread();
        }
    }
}
