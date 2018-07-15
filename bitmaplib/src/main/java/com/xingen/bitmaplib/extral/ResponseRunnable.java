package com.xingen.bitmaplib.extral;

import android.graphics.Bitmap;

import java.util.LinkedList;

/**
 * Created by ${新根} on 2018/7/15.
 * blog博客:http://blog.csdn.net/hexingen
 *
 * 执行任务的请求执行完，将结果更新在，具备相同key且正在等待的全部请求中。
 */

public class ResponseRunnable implements  Runnable{
    private BatchedBitmapRequest batchedBitmapRequest;

    public ResponseRunnable(BatchedBitmapRequest batchedImageRequest) {
        this.batchedBitmapRequest = batchedImageRequest;
    }
    @Override
    public void run() {
        LinkedList<BitmapContainer> mContainers = batchedBitmapRequest.getmContainers();
        for (BitmapContainer imageContainer : mContainers) {
            if (imageContainer.getImageListener() == null) {
                continue;
            }
            Bitmap bitmap=batchedBitmapRequest. getBitmap();
            if (bitmap!= null) {
                imageContainer .setBitmap(bitmap);
                imageContainer.getImageListener().onResponse(imageContainer, false);
            } else {
                imageContainer.getImageListener().onError(batchedBitmapRequest.getError());
            }
        }
    }
}
