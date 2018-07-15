package com.xingen.bitmaplib.internal;

import android.graphics.Bitmap;

import com.xingen.bitmaplib.common.constants.Constants;
import com.xingen.bitmaplib.common.utils.MD5Utils;


/**
 * Author by ${xinGen},  Date on 2018/5/28.
 */
public class BitmapRequest {

    private boolean cancel=false;
    private String imageId;
    private BitmapRequestQueue requestQueue;
    private BitmapListener bitmapListener;
    private int targetWidth,targetHeight;
    private String originId;
    private String tag;
    public BitmapRequest(String imageId,BitmapListener bitmapListener){
        this(imageId,0,0,bitmapListener);
    }
    public BitmapRequest(String imageId, int targetWidth, int targetHeight,BitmapListener bitmapListener) {
        this.imageId = imageId;
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        this.bitmapListener=bitmapListener;
        originId=createOriginId();
    }

    public synchronized boolean isCancel() {
        return cancel;
    }

    public synchronized void setCancel(boolean cancel) {
        this.cancel = cancel;
    }


    public void deliverResult(Bitmap bitmap){
             if (bitmapListener==null||isCancel()){
                 return ;
             }
             bitmapListener.result(originId,bitmap);
    }
    public void deliverError(Exception e){
        if (bitmapListener==null||isCancel()){
            return ;
        }
        bitmapListener.error(originId,e);
    }

    public void finish(){
        this.requestQueue.finishAll(this);
    }

    public void setRequestQueue(BitmapRequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }

    public String getImageId() {
        return imageId;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public int getTargetWidth() {
        return targetWidth;
    }
    /**
     * 获取缓存的Key
     *
     * @return
     */
    public String getCacheKey() {
        return   CacheKeyUtils.createKey(imageId,targetWidth,targetHeight);
    }

    public static class CacheKeyUtils {
        public  static  String createKey(String imageId,int targetWidth,int targetHeight){
            String  s= new StringBuilder(imageId.length() + 12)
                    .append("#W")
                    .append(targetWidth)
                    .append("#H")
                    .append(targetHeight)
                    .append(imageId)
                    .toString();
            return MD5Utils.hashImageUrlForDisk(s);
        }
    }

    private String createOriginId(){
        if (imageId.contains(Constants.PathPrefix.Prefix_Http)) {
            return imageId;
        }else {
            return  Constants.PathPrefix.getActualImageId(imageId);
        }
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }
}
