package com.xingen.bitmaplib.common.policy;

/**
 * Author by {xinGen}
 * Date on 2018/7/26 15:57
 *
 * 图片缓存策略：
 *
 * 1. 指定缓存时间
 * 2. 指定过期时间
 */
public interface CachePolicy {
    /**
     * 当前时间处于，缓存时间，从本地获取
     * @return
     */
    long getCacheTime();

    /**
     * 过期时间，需要重新从网络获取
     * @return
     */
    long getExpiredTime();
}
