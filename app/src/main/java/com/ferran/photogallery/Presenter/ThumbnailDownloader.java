package com.ferran.photogallery.Presenter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int MESSAGE_PRELOAD = 1;

    private Handler mRequestHandler;
    private ConcurrentHashMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHander;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    private LruCache<String, Bitmap> mImageCache = new LruCache<>(1000);

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHander = responseHandler;
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null || url.equals("")) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    public void preloadThumbnail(String url) {
        Log.i(TAG, "preload a image: " + url);
        if (url == null || url.equals("")) {
            return;
        } else {
            mRequestHandler.obtainMessage(MESSAGE_PRELOAD, url).sendToTarget();
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                } else if (msg.what == MESSAGE_PRELOAD) {
                    String url = (String) msg.obj;
                    Log.i(TAG, "Preload Url: " + url);
                    if (url == null || url.equals("")) {
                        return;
                    }
                    try {
                        byte[] bitmapBtyes;
                        final Bitmap bitmap;
                        // add cache layer
                        if (mImageCache.get(url) == null) {
                            bitmapBtyes = new FlickrFetchr().getUrlBytes(url);
                            bitmap = BitmapFactory.decodeByteArray(bitmapBtyes, 0, bitmapBtyes.length);
                            Log.i(TAG, "Bitmap created");
                            mImageCache.put(url, bitmap);
                        }
                        Log.d(TAG, "Preload: " + url);
                    } catch (IOException ioe) {
                        Log.d(TAG, "Cached fail");
                    }
                }
            }
        };
    }

    private void handleRequest(final T target) {
        try {
            final String url = mRequestMap.get(target);

            if (url == null || url.equals("")) {
                return;
            }

            byte[] bitmapBtyes;
            final Bitmap bitmap;
            // add cache layer
            if (mImageCache.get(url) == null) {
                bitmapBtyes = new FlickrFetchr().getUrlBytes(url);
                bitmap = BitmapFactory.decodeByteArray(bitmapBtyes, 0, bitmapBtyes.length);
                Log.i(TAG, "Bitmap created");
                mImageCache.put(url, bitmap);
            } else {
                bitmap = mImageCache.get(url);
                Log.i(TAG, "Cached");
            }
            mResponseHander.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url) {
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }
    }
}
