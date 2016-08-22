package com.ferran.photogallery.Model;

public class GalleryItem {
    private String title;
    private String id;
    private String url_s;

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
}
