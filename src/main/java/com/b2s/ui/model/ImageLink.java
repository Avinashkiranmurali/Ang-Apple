package com.b2s.ui.model;

public class ImageLink {

    private String src;
    private NavigationLink anchorLink;

    public ImageLink() {
    }

    public ImageLink(String src, NavigationLink anchorLink) {
        this.src = src;
        this.anchorLink = anchorLink;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public NavigationLink getAnchorLink() {
        return anchorLink;
    }

    public void setAnchorLink(NavigationLink anchorLink) {
        this.anchorLink = anchorLink;
    }

}
