package com.xingen.bitmaplib.common.net;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.xingen.bitmaplib.common.cache.CacheClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

/**
 * Author by ${xinGen},  Date on 2018/5/28.
 */
public class NetUtils {

    public static void executeRequest(String url, String key, CacheClient cacheClient, final boolean update) throws IOException {
        HttpURLConnection httpURLConnection = url.contains("https") ? (HttpsURLConnection) new URL(url).openConnection() :
                (HttpURLConnection) new URL(url).openConnection();
        CacheHeader cacheHeader = HttpCacheUtils.getCacheHeader(key);
        if (cacheHeader != null) {
            Map<String, String> header = HttpCacheUtils.transform(cacheHeader);
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpURLConnection.addRequestProperty(entry.getKey(), entry.getValue());
            }
            cacheHeader = null;
        }
        httpURLConnection.connect();
        InputStream inputStream = httpURLConnection.getInputStream();
        cacheClient.toDiskLruCache(key, inputStream);
        Map<String, String> responseHeader = parseResponseHeader(httpURLConnection);
        httpURLConnection.disconnect();
        cacheHeader = HttpCacheUtils.transform(responseHeader);
        if (cacheHeader == null) return;
        if (update) {
            HttpCacheUtils.sqlUtil.update(cacheHeader);
        } else {
            HttpCacheUtils.sqlUtil.insert(cacheHeader);
        }
        HttpCacheUtils.addCacheHeader(key, cacheHeader);
    }

    public static Map<String, String> parseResponseHeader(HttpURLConnection connection) {
        Map<String, String> headers = new HashMap<>();
        //添加服务器响应的标头
        for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            if (header.getKey() != null) {
                headers.put(header.getKey(), header.getValue().get(0));
            }
        }
        return headers;
    }


    public static final class HttpCacheUtils {
        private static Map<String, CacheHeader> cacheHeaderMap = new LinkedHashMap<>();
        private static SQLUtil sqlUtil;

        public static void init(Context context) {
            sqlUtil = SQLUtil.getInstance(context);
            List<CacheHeader> cacheHeaderList = sqlUtil.queryAll();
            for (CacheHeader cacheHeader : cacheHeaderList) {
                cacheHeaderMap.put(cacheHeader.key, cacheHeader);
            }
        }

        public static CacheHeader getCacheHeader(String key) {
            return cacheHeaderMap.get(key);
        }

        public static void addCacheHeader(String key, CacheHeader cacheHeader) {
            cacheHeaderMap.put(key, cacheHeader);
        }

        public static void replaceCacheHeader(String key, CacheHeader cacheHeader) {
            cacheHeaderMap.remove(key);
            cacheHeaderMap.put(key, cacheHeader);
        }

        public static CacheHeader transform(String key, Map<String, String> headers) {
            CacheHeader cacheHeader = transform(headers);
            if (cacheHeader != null) {
                cacheHeader.key = key;
            }
            return cacheHeader;
        }

        /**
         * 获取到Http Response中的缓存策略
         *
         * @param headers
         * @return
         */
        public static CacheHeader transform(Map<String, String> headers) {
            long now = System.currentTimeMillis();
            long serverDate = 0;
            long lastModified = 0;
            long serverExpires = 0;
            long softExpire = 0;
            long finalExpire = 0;
            long maxAge = 0;
            long staleWhileRevalidate = 0;
            boolean hasCacheControl = false;
            boolean mustRevalidate = false;
            String serverEtag = null;
            String headerValue;
            headerValue = headers.get("Date");
            if (headerValue != null) {
                serverDate = parseDateAsEpoch(headerValue);
            }
            headerValue = headers.get("Cache-Control");
            if (headerValue != null) {
                hasCacheControl = true;
                String[] tokens = headerValue.split(",", 0);
                for (int i = 0; i < tokens.length; i++) {
                    String token = tokens[i].trim();
                    if (token.equals("no-cache") || token.equals("no-store")) {
                        return null;
                    } else if (token.startsWith("max-age=")) {
                        try {
                            maxAge = Long.parseLong(token.substring(8));
                        } catch (Exception e) {
                        }
                    } else if (token.startsWith("stale-while-revalidate=")) {
                        try {
                            staleWhileRevalidate = Long.parseLong(token.substring(23));
                        } catch (Exception e) {
                        }
                    } else if (token.equals("must-revalidate") || token.equals("proxy-revalidate")) {
                        mustRevalidate = true;
                    }
                }
            }
            headerValue = headers.get("Expires");
            if (headerValue != null) {
                serverExpires = parseDateAsEpoch(headerValue);
            }
            headerValue = headers.get("Last-Modified");
            if (headerValue != null) {
                lastModified = parseDateAsEpoch(headerValue);
            }
            serverEtag = headers.get("ETag");
            if (hasCacheControl) {
                softExpire = now + maxAge * 1000;
                finalExpire = mustRevalidate ? softExpire : softExpire + staleWhileRevalidate * 1000;
            } else if (serverDate > 0 && serverExpires >= serverDate) {
                softExpire = now + (serverExpires - serverDate);
                finalExpire = softExpire;
            }
            CacheHeader cacheHeader = new CacheHeader();
            cacheHeader.refreshTime = softExpire;
            cacheHeader.expireTime = finalExpire;
            cacheHeader.eTag = serverEtag == null ? "" : serverEtag;
            cacheHeader.lastModified = lastModified;
            return cacheHeader;
        }

        /**
         * 将上一次Response的缓存策略告诉后台服务器
         *
         * @param cacheHeader
         * @return
         */
        public static Map<String, String> transform(CacheHeader cacheHeader) {
            if (cacheHeader == null) {
                return Collections.emptyMap();
            }
            Map<String, String> headers = new HashMap<>();
            if (cacheHeader.eTag != null) {
                headers.put("If-None-Match", cacheHeader.eTag);
            }
            if (cacheHeader.lastModified > 0) {
                headers.put("If-Modified-Since", formatEpochAsRfc1123(cacheHeader.lastModified));
            }
            return headers;
        }

        private static long parseDateAsEpoch(String dateStr) {
            try {
                // Parse date in RFC1123 format if this header contains one
                return newRfc1123Formatter().parse(dateStr).getTime();
            } catch (ParseException e) {
                // Date in invalid format, fallback to 0
                return 0;
            }
        }

        private static SimpleDateFormat newRfc1123Formatter() {
            final String RFC1123_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
            SimpleDateFormat formatter = new SimpleDateFormat(RFC1123_FORMAT, Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            return formatter;
        }

        private static String formatEpochAsRfc1123(long epoch) {
            return newRfc1123Formatter().format(new Date(epoch));
        }
    }

    /**
     * 每个请求的http 缓存策略
     */
    public static class CacheHeader {
        /**
         * 存储的key
         */
        public String key;
        /**
         * 缓存时间，过完后需要刷新
         */
        public long refreshTime;
        /**
         * 过期时间，过完后需要重新请求
         */
        public long expireTime;

        public long lastModified;
        public String eTag;

        /**
         * 是否过期
         *
         * @return
         */
        public boolean isExpired() {
            return this.expireTime < System.currentTimeMillis();
        }

        /**
         * 是否在缓存时间
         *
         * @return
         */
        public boolean refreshNeeded() {
            return this.refreshTime < System.currentTimeMillis();
        }
    }

    private static class SQLUtil {
        private static SQLUtil instance;
        private HttpCacheSQL httpCacheSQL;

        private SQLUtil(Context context) {
            httpCacheSQL = new HttpCacheSQL(context);
        }

        public static SQLUtil getInstance(Context context) {
            if (instance == null) {
                instance = new SQLUtil(context);
            }
            return instance;
        }

        public List<CacheHeader> queryAll() {
            List<CacheHeader> cacheHeaderList = new ArrayList<>();
            Cursor cursor = null;
            try {
                SQLiteDatabase database = httpCacheSQL.getWritableDatabase();
                cursor = database.query(DataColumns.TABLE_NAME, null, null, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        CacheHeader cacheHeader = new CacheHeader();
                        cacheHeader.key = cursor.getString(cursor.getColumnIndex(DataColumns.COLUMNS_KEY));
                        cacheHeader.eTag = cursor.getString(cursor.getColumnIndex(DataColumns.COLUMNS_ETAG));
                        cacheHeader.expireTime = Long.valueOf(cursor.getString(cursor.getColumnIndex(DataColumns.COLUMNS_EXPIRE)));
                        cacheHeader.refreshTime = Long.valueOf(cursor.getString(cursor.getColumnIndex(DataColumns.COLUMNS_REFRESH)));
                        cacheHeader.lastModified = Long.valueOf(cursor.getString(cursor.getColumnIndex(DataColumns.COLUMNS_LSATMODIFIED)));
                        cacheHeaderList.add(cacheHeader);
                    } while (cursor.moveToNext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
            return cacheHeaderList;
        }

        public void update(CacheHeader cacheHeader) {
            if (cacheHeader == null) return;
            try {
                SQLiteDatabase database = httpCacheSQL.getWritableDatabase();
                database.update(DataColumns.TABLE_NAME, transform(cacheHeader), DataColumns.COLUMNS_KEY + "=?", new String[]{cacheHeader.key});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private ContentValues transform(CacheHeader cacheHeader) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DataColumns.COLUMNS_KEY, cacheHeader.key);
            contentValues.put(DataColumns.COLUMNS_ETAG, cacheHeader.eTag);
            contentValues.put(DataColumns.COLUMNS_EXPIRE, String.valueOf(cacheHeader.expireTime));
            contentValues.put(DataColumns.COLUMNS_LSATMODIFIED, String.valueOf(cacheHeader.lastModified));
            contentValues.put(DataColumns.COLUMNS_REFRESH, String.valueOf(cacheHeader.refreshTime));
            return contentValues;
        }

        /**
         * 插入某个HttpCache
         *
         * @param cacheHeader
         */
        public void insert(CacheHeader cacheHeader) {
            if (cacheHeader == null) return;
            try {
                SQLiteDatabase database = httpCacheSQL.getWritableDatabase();
                database.insert(DataColumns.TABLE_NAME, null, transform(cacheHeader));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 清空全部数据
         */
        public void deleteAll() {
            try {
                SQLiteDatabase database = httpCacheSQL.getWritableDatabase();
                database.delete(DataColumns.TABLE_NAME, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static class HttpCacheSQL extends SQLiteOpenHelper {
        public static final String CREATE_TABLE = "create table " +
                DataColumns.TABLE_NAME + "(" +
                DataColumns._ID + " integer primary key autoincrement," +
                DataColumns.COLUMNS_KEY + " text," +
                DataColumns.COLUMNS_REFRESH + " text," +
                DataColumns.COLUMNS_EXPIRE + " text," +
                DataColumns.COLUMNS_LSATMODIFIED + " text," +
                DataColumns.COLUMNS_ETAG + " text" + ")";

        public HttpCacheSQL(Context context) {
            super(context, DataColumns.SQL_NAME, null, DataColumns.SQL_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    private final class DataColumns implements BaseColumns {
        private static final String SQL_NAME = "BitmapHttpCache";
        private static final int SQL_VERSION = 1;
        public final static String TABLE_NAME = "HttpCache";
        public final static String COLUMNS_KEY = "bitmapKey";
        public final static String COLUMNS_ETAG = "eTag";
        public final static String COLUMNS_REFRESH = "refresh";
        public final static String COLUMNS_EXPIRE = "expire";
        public final static String COLUMNS_LSATMODIFIED = "lastModified";

    }
}
