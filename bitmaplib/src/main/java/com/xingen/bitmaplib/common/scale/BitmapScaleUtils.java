package com.xingen.bitmaplib.common.scale;

/**
 * Author by ${xinGen},  Date on 2018/5/28.
 */
public class BitmapScaleUtils {

/**
 *
 * 计算合适的压缩值
 *
 * @param targetWidth  指定长度
 * @param targetHeight 指定高度
 * @param actualWidth  图片原始长度
 * @param actualHeight 图片原始高度
 * @return 返回计算好的压缩比例。
 */
public static int calculateBitmapScaleValue(int targetWidth, int targetHeight, int actualWidth, int actualHeight) {
    /**
     *  若是没有指定宽度，同时也没指定高度，则直接加载原始图片的大小，生成Bitmap。
     */
    if (targetWidth == 0 && targetHeight == 0) {
        return 1;
    } else {
        //根据真实的宽高和指定宽高，计算处合适的宽高
        int desiredWidth = getResizedDimension(targetWidth, targetHeight, actualWidth, actualWidth);
        int desiredHeight = getResizedDimension(targetHeight, targetWidth, actualHeight, actualWidth);
        int inSampleSize = findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
        return inSampleSize;
    }
}
    /**
     * 返回一个2的最大的冥除数，用于当做图片的压缩比例，
     * 以确保压缩出来的图片不会超出指定宽高值。
     *
     * @param actualWidth   Actual width of the bitmap
     * @param actualHeight  Actual height of the bitmap
     * @param desiredWidth  Desired width of the bitmap
     * @param desiredHeight Desired height of the bitmap
     */
    private static int findBestSampleSize(int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        //比较两个值，返回最小的值
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }
        return (int) n;
    }

    /**
     * 压缩矩形的一边长度,计算合适的宽高比.
     *
     * @param maxPrimary      最大的主要尺寸
     * @param maxSecondary    最大的辅助尺寸
     * @param actualPrimary   实际主要尺寸
     * @param actualSecondary 实际辅助尺寸
     */
    private static int getResizedDimension(int maxPrimary, int maxSecondary, int actualPrimary, int actualSecondary) {
        // 当最大主要尺寸和最大的辅助尺寸同时为0，无法计算，直接返回实际主要尺寸。
        if (maxPrimary == 0 && maxSecondary == 0) {
            return actualPrimary;
        }
        // 当最大的主要尺寸为0，合适的尺寸=(最大的辅助尺寸/实际辅助尺寸）* 实际主要尺寸
        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }
        //当最大的主要尺寸不为0，最大辅助尺寸为0时，合适的尺寸=最大的主要尺寸。
        if (maxSecondary == 0) {
            return maxPrimary;
        }
        /**
         *
         *  1. 先计算出一个比率=实际最大尺寸/实际辅助尺寸。
         *  2  合适尺寸=比率*最大的主要尺寸
         *  3.若是合适尺寸>最大的辅助尺寸，则合适尺寸=最大的辅助尺寸/比率。
         */
        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;
        if (resized * ratio > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }
}
