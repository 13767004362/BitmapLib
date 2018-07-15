package com.xingen.bitmaplib.common.deliver;

import android.graphics.Bitmap;
import android.os.Handler;

import com.xingen.bitmaplib.internal.BitmapRequest;

import java.util.concurrent.Executor;

/**
 * Author by ${xinGen},  Date on 2018/5/28.
 *
 * 结果传递者，传递异常和bitmap
 *
 */
public class ResultDeliver {
    private MainExecutor mainExecutor;

    public ResultDeliver() {
        this.mainExecutor = new MainExecutor();
    }
    public void executeRunnable(Runnable runnable){
        this.mainExecutor.execute(runnable);
    }
    public void deliverResult(final BitmapRequest request, final Bitmap bitmap){
        this.mainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (request.isCancel()){
                     return;
                }
                request.deliverResult(bitmap);
                request.finish();
            }
        });
    }
    public void deliverError(final BitmapRequest request, final Exception e){
        this.mainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (request.isCancel()){
                    return;
                }
                request.deliverError(e);
                request.finish();
            }
        });
    }
    private static class MainExecutor implements Executor {
        private final Handler handler = new Handler();

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    }

    public static class Result {
        private Bitmap bitmap;
        private Exception exception;

        public Result(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public Result(Exception exception) {
            this.exception = exception;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public Exception getException() {
            return exception;
        }
    }
}
