package com.xingen.bitmaplib.common.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Author by ${xinGen},  Date on 2018/5/25.
 */
public class FileUtils {

    /**
     * 从Asset文件夹下拷贝到指定路径中
     * @param context
     * @param fileName
     * @param desFile
     */
    public static void copyFileFromAssets(Context context, String fileName, File desFile) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = context.getApplicationContext().getAssets().open(fileName);
            out = new FileOutputStream(desFile.getAbsolutePath());
            byte[] bytes = new byte[1024];
            int i;
            while ((i = in.read(bytes)) != -1)
                out.write(bytes, 0, i);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    public static boolean hasExternalStorage() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    /**
     * 获取缓存路径
     *
     * @param context
     * @return 返回缓存文件路径
     */
    public static File getCacheDir(Context context) {
        File cache;
        if (hasExternalStorage()) {
            cache = context.getExternalCacheDir();
        } else {
            cache = getInternalCacheDir(context);
        }
        if (!cache.exists())
            cache.mkdirs();
        return cache;
    }

    /**
     * 获取内部存储
     * @param context
     * @return
     */
    public static  File getInternalCacheDir(Context context){
        return  context.getCacheDir();
    }

    public static File getInternalCacheFile(Context context ,String fileName){
        File dir=getInternalCacheDir(context);
        return  new File(dir+File.separator+fileName);
    }

}
