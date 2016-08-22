package com.ferran.photogallery.Presenter;

import android.support.v4.app.Fragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}
