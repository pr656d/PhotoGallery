package com.practice.premp.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.practice.premp.photogallery.ThumbnailDownloader.ThumbnailDownloadListener;
import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

  public static final String TAG = "PhotoGalleryFragment";

  private RecyclerView mPhotoRecyclerView;
  private List<GalleryItem> mItems = new ArrayList<>();
  private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

  public static PhotoGalleryFragment newInstance() {
    return new PhotoGalleryFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    new FetchItemsTask().execute();

    Handler responseHandler = new Handler();
    mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
    mThumbnailDownloader.setThumbnailDownloadListener(
        new ThumbnailDownloadListener<PhotoHolder>() {
          @Override
          public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            photoHolder.bindDrawable(drawable);
          }
        }
    );
    mThumbnailDownloader.start();
    mThumbnailDownloader.getLooper();
    Log.i(TAG, "Background thread started.");
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
    mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);
    mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

    setupAdapter();

    return v;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mThumbnailDownloader.clearQueue();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mThumbnailDownloader.quit();
    mThumbnailDownloader.clearQueue();
    Log.i(TAG, "Background thread destroyed.");
  }

  private void setupAdapter() {
    if (isAdded()) {    // isAdded() checks for fragment is attached to activity or not.
      mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
    }
  }

  private class PhotoHolder extends RecyclerView.ViewHolder {

    private ImageView mItemImageView;

    public PhotoHolder(View itemView) {
      super(itemView);

      mItemImageView = itemView.findViewById(R.id.item_image_view);
    }

    public void bindDrawable(Drawable drawable) {
      mItemImageView.setImageDrawable(drawable);
    }
  }

  private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

    private List<GalleryItem> mGalleryItems;

    public PhotoAdapter(List<GalleryItem> galleryItems) {
      mGalleryItems = galleryItems;
    }

    @NonNull
    @Override
    public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
      View view = layoutInflater.inflate(R.layout.list_item_gallery,parent,false);

      return new PhotoHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoHolder photoHolder, int position) {
      GalleryItem galleryItem = mGalleryItems.get(position);
      Drawable placeHolder = getResources().getDrawable(R.drawable.my_image);
      photoHolder.bindDrawable(placeHolder);
      mThumbnailDownloader.queueThumbnail(photoHolder, galleryItem.getUrl());
    }

    @Override
    public int getItemCount() {
      return mGalleryItems.size();
    }
  }

  private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

    @Override
    protected List<GalleryItem> doInBackground(Void... params) {

        String query = "robot";

        if (query == null) {
          return new FlickrFetchr().fetchRecentPhots();
        } else {
          return new FlickrFetchr().searchPhotos(query);
        }
    }

    @Override
    protected void onPostExecute(List<GalleryItem> galleryItems) {
      mItems = galleryItems;
      setupAdapter();
    }
  }

}
