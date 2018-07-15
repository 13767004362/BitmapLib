package com.xingen.bitmaplib.extral;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by ${新根} on 2018/7/15.
 * blog博客:http://blog.csdn.net/hexingen
 */

public class ScrollImageView extends ImageView {
    private String imageId;
    private int defaultImageId;
    private int errorImageId;
    private ConcurrentLoader concurrentLoader;
    private BitmapContainer bitmapContainer;
    public ScrollImageView(Context context) {
        super(context);
    }
    public ScrollImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    /**
     * 当ImageView的位置和大小发生改变时候，重新加载图片资源
     *
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        loadBitmapIfNecessary(true);
    }
    /**
     * 当ImageView已经销毁，释放资源。
     */
    @Override
    protected void onDetachedFromWindow() {
        if (bitmapContainer != null) {
            //取消请求
            bitmapContainer.cancelRequest();
            //清空加载的图片
            setImageBitmap(null);
            bitmapContainer = null;
        }
        super.onDetachedFromWindow();
    }
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
    /**
     * 开始加载
     *
     * @param imageId
     * @param concurrentLoader
     */
    public void startImageId(String imageId, ConcurrentLoader concurrentLoader) {
        this.imageId = imageId;
        this.concurrentLoader = concurrentLoader;
        //当imageId发生改变的时候，需要重新加载
        loadBitmapIfNecessary(false);
    }

    /**
     * 是否需要重新加载Bitmap
     *
     * @param isInLayoutPass 当布局加载完成，为true
     */
    private void loadBitmapIfNecessary(final boolean isInLayoutPass) {
        int width = getWidth();
        int height = getHeight();
        //检查ImageView的大小是否包裹内容
        boolean wrapWidth = false, wrapHeight = false;
        if (getLayoutParams() != null) {
            wrapWidth = (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT);
            wrapHeight = (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        /**
         * 当图片的大小是不确定，且没有一个是包裹内容的。
         * 则返回，不进行加载操作。
         */
        boolean isFullyWrapContent = wrapWidth && wrapHeight;
        if (width == 0 && height == 0 && !isFullyWrapContent) {
            return;
        }
        /**
         *
         * 若是新加载的Url是空，则取消上一个请求，且设置当前ImageView的图片为空。
         */
        if (TextUtils.isEmpty(imageId)) {
            if (bitmapContainer != null) {
                bitmapContainer.cancelRequest();
                bitmapContainer= null;
            }
            setDefaultImageOrNull();
            return;
        }
        /**
         * 若是当前存在一个请求，检查新的url和先前的url是否相同。
         */
        if (bitmapContainer != null && bitmapContainer.getImageId() != null) {
            if (bitmapContainer.getImageId().equals(imageId)) {
                return;
            } else {
                bitmapContainer.cancelRequest();
                setDefaultImageOrNull();
            }
        }
        // 计算最大的长/高，若是包裹内容，则设置为0
        int maxWidth = wrapWidth ? 0 : width;
        int maxHeight = wrapHeight ? 0 : height;
        BitmapContainer newContainer = concurrentLoader.loadImage(imageId, maxWidth, maxHeight, new BitmapContainer.BitmapContainerListener() {
            @Override
            public void onResponse(final BitmapContainer response, boolean isImmediate) {
                /**
                 * 当布局还没有加载完成，传递的响应回调，不进行加载到ImageView操作。
                 * 不然会触发一个requestLayout()。
                 *
                 * 这里采用延迟方式，当布局加载完成，再将Bitmap加载到ImageView中。
                 *
                 */
                if (isImmediate && isInLayoutPass) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            onResponse(response, false);
                        }
                    });
                    return;
                }
                if (response.getBitmap() != null) {
                    setImageBitmap(response.getBitmap());
                } else {
                    setImageResource(defaultImageId);
                }
            }
            @Override
            public void onError(Exception error) {
                if (errorImageId != 0) {
                    setImageResource(errorImageId);
                }
            }
        });
        bitmapContainer = newContainer;
    }

    public void setDefaultImageId(int defaultImageId) {
        this.defaultImageId = defaultImageId;
    }

    public void setErrorImageId(int errorImageId) {
        this.errorImageId = errorImageId;
    }
    /**
     * 设置默认图片
     */
    private void setDefaultImageOrNull() {
        if (defaultImageId != 0) {
            setImageResource(defaultImageId);
        } else {
            setImageBitmap(null);
        }
    }
}
