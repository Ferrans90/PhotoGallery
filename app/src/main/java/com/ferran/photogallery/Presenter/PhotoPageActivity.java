package com.ferran.photogallery.Presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.webkit.WebView;

import com.ferran.photogallery.R;

public class PhotoPageActivity extends SingleFragmentActivity {
    public static Intent newIntent(Context context, Uri photoPageUri) {
        Intent i = new Intent(context, PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        return PhotoPageFragment.newInstance(getIntent().getData());
    }

    @Override
    public void onBackPressed() {
        PhotoPageFragment fragment = (PhotoPageFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        WebView mWebView = fragment.getWebView();
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }


    }
}
