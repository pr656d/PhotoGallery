package com.practice.premp.photogallery;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {

  public static final String TAG = "PhotoGalleryFragment";

  private RecyclerView mPhotoRecyclerView;
  private List<GalleryItem> mItems = new ArrayList<>();

  public static PhotoGalleryFragment newInstance() {
    return new PhotoGalleryFragment();
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    new FetchItemsTask().execute();
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

  private void setupAdapter() {
    if (isAdded()) {    // isAdded() checks for fragment is attached to activity or not.
      mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
    }
  }

  private class PhotoHolder extends RecyclerView.ViewHolder {

    private ImageView mItemImageView;

    public PhotoHolder(View itemView) {
      super(itemView);

      mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
    }

    public void bindGalleryItem(Drawable drawable) {
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
    public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
      GalleryItem galleryItem = mGalleryItems.get(position);
      Drawable placeHolder = getResources().getDrawable(R.drawable.my_image);
      holder.bindGalleryItem(placeHolder);
    }

    @Override
    public int getItemCount() {
      return mGalleryItems.size();
    }
  }

  private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

    @Override
    protected List<GalleryItem> doInBackground(Void... params) {
      return new FlickrFetchr().fetchItems();
    }

    @Override
    protected void onPostExecute(List<GalleryItem> galleryItems) {
      mItems = galleryItems;
      setupAdapter();
    }
  }

}
