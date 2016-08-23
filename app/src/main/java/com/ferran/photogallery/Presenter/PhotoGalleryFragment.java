package com.ferran.photogallery.Presenter;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ferran.photogallery.Model.GalleryItem;
import com.ferran.photogallery.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private PhotoAdapter mPhotoAdapter;
    private int lastPosition = 100;

    public PhotoGalleryFragment() {
        // Required empty public constructor
    }

    public static PhotoGalleryFragment newInstance() {

        Bundle args = new Bundle();

        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                int lastPosition = gridLayoutManager.findLastCompletelyVisibleItemPosition();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && lastPosition + 1 == mItems.size()) {
                    new FetchItemsTask().execute();
                    updateUI();
                }
            }
        });
        //setupAdapter();
        updateUI();
        return v;
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private void updateUI() {
        if (mPhotoAdapter == null) {
            mPhotoAdapter = new PhotoAdapter(mItems);
            mPhotoRecyclerView.setAdapter(mPhotoAdapter);
        } else {
            mPhotoAdapter.setGalleryItems(mItems);
            mPhotoAdapter.notifyItemRangeInserted(lastPosition, 100);
            lastPosition += 100;
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            return new FlickrFetchr().fetchItem();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems.addAll(galleryItems);
            //setupAdapter();
            updateUI();
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private TextView mTitleView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mTitleView = (TextView) itemView;
        }

        public void bindGalleryItem(GalleryItem item) {
            mTitleView.setText(item.toString());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        private void setGalleryItems(List<GalleryItem> items) {
            mGalleryItems = items;
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            holder.bindGalleryItem(galleryItem);
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }
    }

}
