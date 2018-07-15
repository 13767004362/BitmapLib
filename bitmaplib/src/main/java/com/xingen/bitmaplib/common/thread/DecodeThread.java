package com.xingen.bitmaplib.common.thread;

import android.graphics.Bitmap;
import android.os.Process;

import com.xingen.bitmaplib.common.decode.BitmapDecode;
import com.xingen.bitmaplib.common.deliver.ResultDeliver;
import com.xingen.bitmaplib.internal.BitmapRequest;

import java.util.concurrent.BlockingQueue;

/**
 * Author by ${xinGen},  Date on 2018/5/28.
 */
public class DecodeThread extends Thread {
    public volatile boolean stop = false;
    private final BlockingQueue<BitmapRequest> requestQueue;
    private final ResultDeliver resultDeliver;
    private final BitmapDecode decode;
    public DecodeThread(BlockingQueue<BitmapRequest> requestQueue, BitmapDecode decode, ResultDeliver deliver) {
        this.requestQueue = requestQueue;
        this.decode = decode;
        this.resultDeliver = deliver;
    }
    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            BitmapRequest request;
            try {
                request = requestQueue.take();
            } catch (InterruptedException e) {
                if (stop) {
                    return;
                }
                continue;
            }
            if (request.isCancel()) {
                  continue;
            }
            try {
               Bitmap bitmap= decode.decode(request.getImageId(),request.getTargetWidth(),request.getTargetHeight());
                if (request.isCancel()) {
                     continue;
                }
               if (bitmap==null){
                   resultDeliver.deliverError(request,new Exception("Bitmap 加载失败"));
               }else{
                   resultDeliver.deliverResult(request,bitmap);
               }
            }catch (Exception e){
                resultDeliver.deliverError(request,e);
            }
        }

    }
    public void stopThread() {
        stop = true;
        interrupt();
    }
}
