package com.practice.premp.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollService extends IntentService {

  public static final String TAG = "PollService";

  // Set interval to 1 min.
  private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(15);

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
    Log.i(TAG, "onHandleIntent: called");
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
      Log.i(TAG, "Got old result: " + resultId);
    } else {
      Log.i(TAG, "Got a new result: " + resultId);

      Resources resources = getResources();
      Intent i = PhotoGalleryActivity.newIntent(this);
      PendingIntent pi = PendingIntent.getService(this, 0, i, 0);

      // Notification won't work in android 8.0 or above due to background service not allowed.
      // Also NotificationCompat is deprecated so it's slightly changed than written in book.
      Notification notification = new NotificationCompat.Builder(this, "123")
          .setTicker(resources.getString(R.string.new_pictures_title))
          .setSmallIcon(android.R.drawable.ic_menu_report_image)
          .setContentTitle(resources.getString(R.string.new_pictures_title))
          .setContentText(resources.getString(R.string.new_pictures_text))
          .setContentIntent(pi)
          .setAutoCancel(true)
          .build();

      NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
      notificationManager.notify(0, notification);

      Log.i(TAG, "NOTIFICATION is notified");
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