package com.xingen.bitmaplib.common.constants;

/**
 * Author by ${xinGen},  Date on 2018/5/28.
 */
public final class Constants {

    /**
     * 插件的名字
     */
    public static final String PLUGIN_NAME = "libVCore.zip";

    /**
     * 图片路径构建类
     */
    public static class  ImageIdBuilder{
        public static String createDrawable(int drawableId){
            return  PathPrefix.Prefix_Drawable+drawableId;
        }
        public  static String createMipmap(int mipmapId ){
            return  PathPrefix.Prefix_Mipmap+mipmapId;
        }
        public  static String createFile(String filePth ){
            return  PathPrefix.Prefix_File+filePth;
        }
        public  static String createAsset(String fileName){
            return  PathPrefix.Prefix_Asset+fileName;
        }
    }

    /**
     * 线程的配置类
     */
    public static final  class  ThreadConstants{
        public static final int thread_size=3;
    }
    /**
     * 路径的前缀
     */
    public static  final  class PathPrefix{
        public static final  String Prefix_Drawable="drawable://";
        public static final  String Prefix_Mipmap="mipmap://";
        public static final  String Prefix_File="file://";
        public static final  String Prefix_Asset="asset://";
        public static final  String Prefix_Http="http://";
        public static final  String Prefix_Https="https://";

        /**
         * 去除前缀，获取到真实路径
         * @param imageId
         * @return
         */
        public static  String getActualImageId(String imageId){
            return  imageId.split("//")[1].trim();
        }
    }
}