package com.practice.premp.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollService extends IntentService {

  public static final String TAG = "PollService";

  // Set interval to 1 min.
  private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);

  public static Intent newIntent(Context context) {
    return new Intent(context, PollService.class);
  }

  public static void setServiceAlarm(Context context, boolean isOn) {
    Intent i = PollService.newIntent(context);
    PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

    AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);

    if (isOn) {
      alarmManager.setRepeating(
          AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL_MS, pi);
    } else {
      alarmManager.cancel(pi);
      pi.cancel();
    }
  }

  public static boolean isServiceAlarmOn(Context context) {
    Intent i = PollService.newIntent(context);
    // If PendingIntent does not already exist then returns null instead of creating it.
    PendingIntent pi = PendingIntent
        .getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
    return pi != null;
  }

  public PollService() {
    super(TAG);
  }

  @Override
  protected void onHandleIntent(@Nullable Intent intent) {
    if(!isNetworkAvailableAndConnected()) {
      return;
    }

    String query = QueryPreferences.getStoredQuery(this);
    String lastResultId = QueryPreferences.getLastResultId(this);
    List<GalleryItem> items;

    if (query == null) {
      items = new FlickrFetchr().fetchRecentPhotos();
    } else {
      items = new FlickrFetchr().searchPhotos(query);
    }

    if (items.size() == 0)
      return;

    String resultId = items.get(0).getId();
    if (resultId.equals(lastResultId)) {
      Log.i(TAG, "Got and old result: " + resultId);
    } else {
      Log.i(TAG, "Got a new result: " + resultId);
    }

    QueryPreferences.setLastResultId(this, resultId);

    Log.d(TAG, "Received an intent: " + intent);
  }

  private boolean isNetworkAvailableAndConnected() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

    boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
    boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

    return isNetworkConnected;
  }
}