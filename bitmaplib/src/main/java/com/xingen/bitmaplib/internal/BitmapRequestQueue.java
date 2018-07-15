package com.xingen.bitmaplib.internal;

import android.graphics.Bitmap;

import com.xingen.bitmaplib.common.cache.CacheClient;
import com.xingen.bitmaplib.common.constants.Constants;
import com.xingen.bitmaplib.common.decode.BitmapDecode;
import com.xingen.bitmaplib.common.deliver.ResultDeliver;
import com.xingen.bitmaplib.common.thread.DecodeThread;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Author by ${xinGen},  Date on 2018/5/28.
 */
public class BitmapRequestQueue {
    /**
     * 线程需要执行的请求
     */
    private final BlockingQueue<BitmapRequest> decodeThreadQueue = new LinkedBlockingQueue<>();
    /**
     * 存储相同key的请求
     */
    private final Map<String, Queue<BitmapRequest>> waitingRequestQueue = new HashMap<>();
    /**
     * 当前全部的请求队列
     */
    private final Set<BitmapRequest> currentRequestQueue = new HashSet<>();

    private BitmapDecode bitmapDecode;
    private CacheClient cacheClient;
    private DecodeThread[]  decodeThreads;
    private ResultDeliver resultDeliver;
    public BitmapRequestQueue(BitmapDecode bitmapDecode, CacheClient cacheClient) {
        this.bitmapDecode = bitmapDecode;
        this.cacheClient = cacheClient;
        this.resultDeliver=new ResultDeliver();
        this.decodeThreads=new DecodeThread[Constants.ThreadConstants.thread_size];
        for (int i=0;i<decodeThreads.length;++i){
            decodeThreads[i]=new DecodeThread(this.decodeThreadQueue,this.bitmapDecode,this.resultDeliver);
            decodeThreads[i].start();
        }
    }
    public void addRequest(BitmapRequest request){
        request.setRequestQueue(this);
        synchronized (currentRequestQueue){
            currentRequestQueue.add(request);
        }
        synchronized (waitingRequestQueue){
                String cacheKey=request.getCacheKey();
                 if (waitingRequestQueue.containsKey(cacheKey)){
                     Queue<BitmapRequest> stagedRequests = waitingRequestQueue.get(cacheKey);
                     if (stagedRequests == null) {
                         stagedRequests = new LinkedList<>();
                     }
                     //加入同一个等待执行的请求队列中
                     stagedRequests.add(request);
                     waitingRequestQueue.put(cacheKey, stagedRequests);
                 }else{
                    this. decodeThreadQueue.add(request);
                    this.waitingRequestQueue.put(cacheKey,null);
                 }
        }

    }
    public void finishAll(BitmapRequest bitmapRequest){
        synchronized (currentRequestQueue){
            currentRequestQueue.remove(bitmapRequest);
        }
        synchronized (waitingRequestQueue){
            final Queue<BitmapRequest> waitRequests = waitingRequestQueue.remove(bitmapRequest.getCacheKey());
            if (waitRequests != null) {
                this.resultDeliver.executeRunnable(new ResultRunnable(waitRequests,this.cacheClient));
            }
        }
    }

    /**
     * 执行相同url和同样大小的request，从内存读取结果。
     */
    private static class  ResultRunnable implements Runnable {
        private Queue<BitmapRequest> bitmapRequests;
        private CacheClient cacheClient;

        public ResultRunnable(Queue<BitmapRequest> bitmapRequests, CacheClient cacheClient) {
            this.bitmapRequests = bitmapRequests;
            this.cacheClient = cacheClient;
        }

        @Override
        public void run() {
            try {
                for (BitmapRequest request : this.bitmapRequests) {
                    if (request != null && !request.isCancel()) {
                        Bitmap bitmap = cacheClient.fromLruCache(request.getCacheKey());
                        if (bitmap != null) {
                            request.deliverResult(bitmap);
                        } else {
                            request.deliverError(new Exception("Bitmap is null"));
                        }
                    }
                }
                this.bitmapRequests.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public  void stopThread(){
        if (decodeThreads!=null){
            for (DecodeThread decodeThread:decodeThreads){
                decodeThread.stopThread();
            }
        }
    }

    /**
     * 根据Tag取消
     *
     * @param tag
     */
    public void cancelAll(final Object tag) {
        if (tag == null) {
            return;
        }
        cancelAll(new RequestFilter() {
            @Override
            public boolean apply(BitmapRequest request) {
                return tag == request.getTag();
            }
        });
    }
    /**
     * 根据过滤器来取消
     *
     * @param requestFilter
     */
    public void cancelAll(RequestFilter requestFilter) {
        synchronized (currentRequestQueue) {
            for (BitmapRequest request : currentRequestQueue) {
                if (requestFilter.apply(request)) {
                    currentRequestQueue.remove(request);
                    request.setCancel(true);
                }
            }
        }
    }
    /**
     * 请求的过滤
     */
    public interface RequestFilter {
        boolean apply(BitmapRequest request);
    }
}
