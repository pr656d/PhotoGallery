package com.practice.premp.photogallery;

import android.os.HandlerThread;
import android.util.Log;

public class ThumbnailDownloader<T> extends HandlerThread {
  public static final String TAG = "Thumbnail Downloader";

  private boolean mHasQuit = false;

  public ThumbnailDownloader() {
    super(TAG);
  }

  @Override
  public boolean quit() {
    mHasQuit = true;
    return super.quit();
  }

  public void queueThumbnail(T target, String url) {
    Log.i(TAG, "Got a URL: " + url);
  }
}
