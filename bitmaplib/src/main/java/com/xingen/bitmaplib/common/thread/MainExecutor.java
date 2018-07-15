package com.xingen.bitmaplib.common.thread;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;


/**
 * Author by ${xinGen},  Date on 2018/5/28.
 */
public class MainExecutor implements Executor {
    private final Handler handler=new Handler(Looper.getMainLooper()) ;
    @Override
    public void execute(Runnable command) {
        handler.post(command);
    }
}
