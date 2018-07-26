package com.xingen.bitmaplib.common.policy;

/**
 * Author by {xinGen}
 * Date on 2018/7/26 16:01
 */
public class DefaultCachePolicy implements CachePolicy {
    /**
     * 多少秒内处于缓存中，过完需要刷新
     */
    protected int refresh;
    /**
     * 多少秒后过期
     */
    protected int expire;
    /**
     * 默认三分钟内
     */
    private static final int default_refresh=3*60;
    /**
     * 默认三小时候过期
     */
    private static final int default_expire=3*60*60;

    public DefaultCachePolicy() {
       this(default_refresh,default_expire);
    }
    public DefaultCachePolicy(int refresh, int expire) {
        this.refresh = refresh;
        this.expire = expire;
    }
    @Override
    public long getCacheTime() {
        long currentTime=System.currentTimeMillis();
        return currentTime+refresh*1000;
    }
    @Override
    public long getExpiredTime() {
        long currentTime=System.currentTimeMillis();
        return currentTime+expire*1000;
    }
}
