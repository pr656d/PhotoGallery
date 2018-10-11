package com.practice.premp.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;

public abstract class VisibleFragment extends Fragment {

  /**
   *
   * This will register and receive notification intent.
   * If app is alive it's no need to show notification and simply discard it.
   *
   * */


  public static final String TAG = "VisibleFragment";

  @Override
  public void onStart() {
    super.onStart();
    IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
    getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);
  }

  @Override
  public void onStop() {
    super.onStop();
    getActivity().unregisterReceiver(mOnShowNotification);
  }

  private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      // If we receive this, we're visible, so cancel the notification.
      Log.i(TAG, "Canceling notification");
      setResultCode(Activity.RESULT_CANCELED);
    }
  };
}
