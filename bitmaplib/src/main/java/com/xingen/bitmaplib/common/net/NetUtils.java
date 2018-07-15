package com.xingen.bitmaplib.common.net;


import com.xingen.bitmaplib.common.cache.CacheClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Author by ${xinGen},  Date on 2018/5/28.
 */
public class NetUtils {

    public static void executeRequest(String url, String key,CacheClient cacheClient) throws IOException {
        HttpURLConnection httpURLConnection = url.contains("https") ? (HttpsURLConnection) new URL(url).openConnection() :
                (HttpURLConnection) new URL(url).openConnection();
        httpURLConnection.connect();
        cacheClient.toDiskLruCache(key, httpURLConnection.getInputStream());
        httpURLConnection.disconnect();
    }
}
