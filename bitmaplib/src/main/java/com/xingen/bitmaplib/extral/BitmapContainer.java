package com.xingen.bitmaplib.extral;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by ${新根} on 2018/7/15.
 * blog博客:http://blog.csdn.net/hexingen
 *
 *  图片信息的容器类：
 *  1. 存储一系列的数据
 *  2. 取消当前这个请求
 *  3. 加载默认图片，异常图片，请求的图片
 */

public class BitmapContainer {
    private Bitmap bitmap;
    private String cacheKey;
    private String imageId;
    private BitmapContainerListener imageListener;
    private ConcurrentLoader concurrentLoader;
    public BitmapContainer(ConcurrentLoader concurrentLoader,Bitmap bitmap, String cacheKey, String imageId, BitmapContainerListener imageListener) {
        this.concurrentLoader= concurrentLoader;
        this.bitmap = bitmap;
        this.cacheKey = cacheKey;
        this.imageId = imageId;
        this.imageListener = imageListener;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public String getImageId() {
        return imageId;
    }
    public void cancelRequest(){
        if (concurrentLoader!=null){
            concurrentLoader.cancelRequest(this);
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public BitmapContainerListener getImageListener() {
        return imageListener;
    }

    /**
     * 处理图片加载成功和异常的接口。
     */
    public interface BitmapContainerListener {
        void onResponse(BitmapContainer response, boolean isImmediate);

        void onError(Exception error);
    }

    /**
     * 创建一个BitmapContainerListener
     *
     * @param view
     * @param defaultImageResId
     * @param errorImageResId
     * @return
     */
    public static BitmapContainerListener createImageListener(final ImageView view, final int defaultImageResId, final int errorImageResId) {
        return new BitmapContainerListener() {
            @Override
            public void onResponse(BitmapContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    view.setImageBitmap(response.bitmap);
                } else {
                    if (defaultImageResId != 0) {
                        view.setImageResource(defaultImageResId);
                    }
                }
            }
            @Override
            public void onError(Exception error) {
                if (errorImageResId != 0) {
                    view.setImageResource(errorImageResId);
                }
            }
        };
    }
}
