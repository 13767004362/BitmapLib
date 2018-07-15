package com.xingen.bitmaplib.common.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.xingen.bitmaplib.common.scale.BitmapScaleUtils;
import com.xingen.bitmaplib.common.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Author by ${xinGen},  Date on 2018/5/28.
 */
public class CacheClient {
    private BitmapLruCache lruCache;
    private DiskLruCache diskLruCache;

    public CacheClient(Context context) {
        this.lruCache = new BitmapLruCache();
        File directory = new File(FileUtils.getInternalCacheDir(context) + File.separator + CacheConfig.BITMAP_CACHE_FILE);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        int appVersion = CacheConfig.VERSON;
        int valueCount = CacheConfig.FILECOUNT;
        long maxSize = CacheConfig.BITMAP_SIZE;
        try {
            this.diskLruCache = DiskLruCache.open(directory, appVersion, valueCount, maxSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toLruCache(String key, Bitmap bitmap) {
        this.lruCache.put(key, bitmap);
    }

    public Bitmap fromLruCache(String key) {
        return this.lruCache.get(key);
    }

    /**
     * 从磁盘中获取图片
     *
     * @param key
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public Bitmap fromDiskLruCache(String key, int targetWidth, int targetHeight) {
        Bitmap bitmap = null;
        InputStream inputStream;
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
            if (snapshot != null) {
                Rect rect = new Rect();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                inputStream = snapshot.getInputStream(0);
                BitmapFactory.decodeStream(inputStream, rect, options);
                if (inputStream != null) {
                    inputStream.close();
                }
                snapshot = diskLruCache.get(key);
                options.inSampleSize = BitmapScaleUtils.calculateBitmapScaleValue(targetWidth, targetHeight, options.outWidth, options.outHeight);
                options.inJustDecodeBounds = false;
                inputStream = snapshot.getInputStream(0);
                bitmap = BitmapFactory.decodeStream(inputStream, rect, options);
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 移除图片
     *
     * @param key
     */
    public void deleteDiskLruCache(String key) {
        try {
            this.diskLruCache.remove(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加stream到磁盘中
     *
     * @param key
     * @param inputStream
     */
    public void toDiskLruCache(String key, InputStream inputStream) throws IOException {
        DiskLruCache.Editor editor = this.diskLruCache.edit(key);
        if (writerIO(inputStream, editor.newOutputStream(0))) {
            editor.commit();
        } else {
            editor.abort();
        }
        // 同步到日志
        this.diskLruCache.flush();

    }

    private boolean writerIO(InputStream inputStream, OutputStream outputStream) {
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(outputStream);
            bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] bytes = new byte[10 * 1024];
            int count;
            while ((count = bufferedInputStream.read(bytes)) != -1) {
                bufferedOutputStream.write(bytes, 0, count);
            }
            bufferedOutputStream.flush();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (bufferedOutputStream != null) {
                    bufferedInputStream.close();
                }
            } catch (Exception e) {

            }

        }
        return true;
    }


    /**
     * DiskLruCache的配置
     */
    public static final class CacheConfig {
        //允许10M bitmap值
        public static final int BITMAP_SIZE = 10 * 1024 * 1024;
        //bitmap储存的目录
        public static final String BITMAP_CACHE_FILE = "bitmapCacheFile";
        //允许的文件个数
        public static final int FILECOUNT = 1;
        //版本
        public static final int VERSON = 1;
    }
}
