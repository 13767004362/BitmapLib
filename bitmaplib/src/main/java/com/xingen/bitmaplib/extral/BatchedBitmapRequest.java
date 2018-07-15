package com.xingen.bitmaplib.extral;

import android.graphics.Bitmap;

import com.xingen.bitmaplib.internal.BitmapRequest;

import java.util.LinkedList;

/**
 * Created by ${新根} on 2018/7/15.
 * blog博客:http://blog.csdn.net/hexingen
 *
 * 一个存储相同key的队列类，存储执行任务的请求，结果（Bitmap，error）,队列。
 *
 */

public class BatchedBitmapRequest {
    /**
     * 第一个去加载原资源的请求
     */
    private final BitmapRequest request;
    private Bitmap bitmap;
    private Exception error;
    //用于存储相同key的BitmapContainer
    private final LinkedList<BitmapContainer> mContainers = new LinkedList<>();

    public BatchedBitmapRequest(BitmapRequest request, BitmapContainer container) {
        this.request = request;
        this.mContainers.add(container);
    }

    public LinkedList<BitmapContainer> getmContainers() {
        return mContainers;
    }

    public void setError(Exception error) {
        this.error = error;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public Exception getError() {
        return error;
    }
    public  void addContainer(BitmapContainer container){
        this.mContainers.add(container);
    }
    /**
     * 从等待回调的Request队列中移除,
     * 若是该请求正在执行，则取消。
     *
     * @param container
     * @return
     */
    public boolean removeContainerAndCancelIfNecessary(BitmapContainer container) {
        this.mContainers.remove(container);
        if (mContainers.size() == 0) {
            request.setCancel(true);
            return true;
        }
        return false;
    }
}
