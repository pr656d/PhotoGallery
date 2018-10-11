package com.practice.premp.photogallery;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class PhotoPageFragment extends VisibleFragment {
  private static final String ARG_URI = "photo_page_url";

  private Uri mUri;
  private WebView mWebView;
  private ProgressBar mProgressBar;

  public static PhotoPageFragment newInstance(Uri uri) {
    Bundle args = new Bundle();
    args.putParcelable(ARG_URI, uri);

    PhotoPageFragment fragment = new PhotoPageFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mUri = getArguments().getParcelable(ARG_URI);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_photo_page, container, false);

    mProgressBar = v.findViewById(R.id.progress_bar);
    mProgressBar.setMax(100); // WebChromeClient reports in range 0-100

    mWebView = v.findViewById(R.id.web_view);
    mWebView.getSettings().setJavaScriptEnabled(true);
    mWebView.setWebViewClient(new WebViewClient());
    mWebView.loadUrl(mUri.toString());

    mWebView.setWebChromeClient(new WebChromeClient() {
      public void onProgressChanged(WebView view, int newProgress) {
        if (newProgress == 100) {
          mProgressBar.setVisibility(View.GONE);
        } else {
          mProgressBar.setVisibility(View.VISIBLE);
          mProgressBar.setProgress(newProgress);
        }
      }

      public void onReceivedTitle(WebView view, String title) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(title);
      }
    });

    return v;
  }
}
