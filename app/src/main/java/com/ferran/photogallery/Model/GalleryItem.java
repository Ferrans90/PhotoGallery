package com.ferran.photogallery.Model;

import android.net.Uri;

public class GalleryItem {
    private String title;
    private String id;
    private String url_s;
    private String owner;

    @Override
    public String toString() {
        return title;
    }

    public String getCaption() {
        return title;
    }

    public void setCaption(String mCaption) {
        this.title = mCaption;
    }

    public String getId() {
        return id;
    }

    public void setId(String mId) {
        this.id = mId;
    }

    public String getUrl() {
        return url_s;
    }

    public void setUrl(String mUrl) {
        this.url_s = mUrl;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Uri getPhotoPageUri() {
        return Uri.parse("http://www.flickr.com/photos/")
            .buildUpon().appendPath(owner).appendPath(id).build();

    }
}
