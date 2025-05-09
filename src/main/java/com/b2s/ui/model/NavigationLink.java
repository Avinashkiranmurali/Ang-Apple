package com.b2s.ui.model;

/**
 * @author dmontoya
 * @version 1.0, 11/16/12 4:28 PM
 * @since b2r-rewardstep 5.2
 */
public class NavigationLink {

    public enum Target {
        BLANK("_blank"), SELF("_self"), PARENT("_parent"), TOP("_top"), NEW("_new");
        private final String value;
        Target(final String val) {
            this.value = val;
        }
        public String getValue() {
            return value;
        }
        public static Target fromValue(final String val) {
            for (final Target tg:Target.values()) {
                if (tg.getValue().equals(val))
                    return tg;
            }
            throw new IllegalArgumentException("invalid Target value: "+ val);
        }
    }

    private String name;
    private String url;
    private Target target;
    private boolean active;
    private String imageUrl;
    /**
     * Image to display when this link is selected.
     */
    private String selectedImageUrl;

    public NavigationLink(final String name, final String url) {
        this.name = name;
        this.url = url;
    }

    public NavigationLink(final String name, final String url, final String imageUrl) {
        this.name = name;
        this.url = url;
        this.imageUrl = imageUrl;
    }

    public NavigationLink(final String name, final String url, final String imageUrl, final String selectedImageUrl) {
        this.name = name;
        this.url = url;
        this.imageUrl = imageUrl;
        this.selectedImageUrl = selectedImageUrl;
    }

    public NavigationLink(final String name, final String url, final Target target) {
        this.name = name;
        this.url = url;
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSelectedImageUrl() {
        return selectedImageUrl;
    }

    public void setSelectedImageUrl(final String selectedImageUrl) {
        this.selectedImageUrl = selectedImageUrl;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("NavigationLink{").append("name='").append(name).append('\'').append(", url='").append(url).append('\'').append(", target=").append(target).append('}').toString();
    }
}
