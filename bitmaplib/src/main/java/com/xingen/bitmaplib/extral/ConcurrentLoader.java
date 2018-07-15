package com.xingen.bitmaplib.extral;

import android.graphics.Bitmap;

import com.xingen.bitmaplib.common.cache.CacheClient;
import com.xingen.bitmaplib.common.thread.MainExecutor;
import com.xingen.bitmaplib.common.utils.ProcessUtils;
import com.xingen.bitmaplib.internal.BitmapListener;
import com.xingen.bitmaplib.internal.BitmapRequest;
import com.xingen.bitmaplib.internal.BitmapRequestQueue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ${新根} on 2018/7/15.
 * blog博客:http://blog.csdn.net/hexingen
 * <p>
 * 用于处理并发加载情况：使用ImageView 或者滚动列表
 */

public class ConcurrentLoader {
    /**
     * 主线程执行工具
     */
    private static final MainExecutor mainExecutor = new MainExecutor();
    /**
     * 存储相同key,正在执行的BitmapRequest
     */
    private final Map<String, BatchedBitmapRequest> inFlightRequestQueue = new HashMap<>();
    private final BitmapRequestQueue requestQueue;
    private CacheClient cache;
    public ConcurrentLoader(BitmapRequestQueue requestQueue, CacheClient cache) {
        this.requestQueue = requestQueue;
        this.cache = cache;
    }
    /**
     * 检查内存中是否缓存(指定资源的)Bitmap
     *
     * @param imageId
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public boolean isCached(String imageId, int targetWidth, int targetHeight) {
        //调用在主线程中
        ProcessUtils.throwIfNotOnMainThread();
        return cache.fromLruCache(BitmapRequest.CacheKeyUtils.createKey(imageId, targetWidth, targetHeight)) != null ? true : false;
    }
    public BitmapContainer loadImage(String imageId, BitmapContainer.BitmapContainerListener imageListener) {
        return loadImage(imageId, 0, 0, imageListener);
    }
    public BitmapContainer loadImage(String imageId, int targetWidth, int targetHeight, BitmapContainer.BitmapContainerListener imageListener) {
        //调用在主线程中
        ProcessUtils.throwIfNotOnMainThread();
        //先从内存中获取，若是存在，直接UI更新图片
        final String cacheKey = BitmapRequest.CacheKeyUtils.createKey(imageId, targetWidth, targetHeight);
        Bitmap bitmap = cache.fromLruCache(cacheKey);
        if (bitmap != null) {
            BitmapContainer container = new BitmapContainer(this, bitmap, cacheKey, imageId, imageListener);
            //立马回调更新操作
            imageListener.onResponse(container, true);
            return container;
        }
        //加入请求队列
        BitmapContainer container = new BitmapContainer(this, null, cacheKey, imageId, imageListener);
        //先加载预览图片
        imageListener.onResponse(container, true);
        // 检查具备相同url的请求，是否已经在执行.
        BatchedBitmapRequest batchedBitmapRequest = inFlightRequestQueue.get(cacheKey);
        if (batchedBitmapRequest != null) {
            //已经存在相同key的Request在执行，则添加等待响应的集合中。
            batchedBitmapRequest.addContainer(container);
            return container;
        }
        BitmapRequest request = new BitmapRequest(imageId, targetWidth, targetHeight, new BitmapListener() {
            @Override
            public void result(String imageId, Bitmap bitmap) {
                handleSuccess(cacheKey, bitmap);
            }

            @Override
            public void error(String imageId, Exception e) {
                handleError(cacheKey, e);
            }
        });
        //开始执行任务请求
        requestQueue.addRequest(request);
        //记录每个相同key的容器
        inFlightRequestQueue.put(cacheKey, new BatchedBitmapRequest(request, container));
        return container;
    }
    public void handleSuccess(String cacheKey, Bitmap response) {
        BatchedBitmapRequest batchedBitmapRequest = inFlightRequestQueue.remove(cacheKey);
        if (batchedBitmapRequest != null) {
            batchedBitmapRequest.setBitmap(response);
            mainExecutor.execute(new ResponseRunnable(batchedBitmapRequest));
        }
    }
    public void handleError(String cacheKey, Exception e) {
        BatchedBitmapRequest batchedBitmapRequest = inFlightRequestQueue.remove(cacheKey);
        if (batchedBitmapRequest != null) {
            batchedBitmapRequest.setError(e);
            mainExecutor.execute(new ResponseRunnable(batchedBitmapRequest));
        }
    }
    public void cancelRequest(BitmapContainer bitmapContainer) {
        final String cacheKey = bitmapContainer.getCacheKey();
        BatchedBitmapRequest batchedBitmapRequest = inFlightRequestQueue.get(cacheKey);
        if (batchedBitmapRequest != null) {
            //若是已经没有BitmapContainer需要响应，则移除相应的batchedBitmapRequest
            boolean isLast = batchedBitmapRequest.removeContainerAndCancelIfNecessary(bitmapContainer);
            if (isLast) {
                inFlightRequestQueue.remove(batchedBitmapRequest);
            }
        }
    }
}
