package com.b2s.ui;

import com.b2s.ui.model.ImageLink;
import com.b2s.ui.model.NavigationLink;

import java.util.List;

/**
 * @author dmontoya
 * @version 1.0, 5/1/13 10:42 AM
 * @since b2r-rewardstep 6.0
 */
public class NavigationConfig {

    /**
     * top header banner
     */
    private ImageLink header;
    /**
     * top header banner for Travel
     */
    private ImageLink travelHeader;
    /**
     * VAR custom top navigation menu.
     * This menu is only for VARs specific links, they are not related to b2s neither to any store front.
     * The VAR menu has nothing to do with the custom store front links!
     */
    private List<NavigationLink> varMenuLinks;

    /**
     * VAR custom store links.
     * TODO move custom store links here
     */
    private List<NavigationLink> customStoreLinks;

    /**
     * Determines if the var-program uses the home landing or the users land directly to the store front. Default value
     * is true.
     */
    private boolean homeLandingEnabled = true;

    /**
     * Determines if a link to the viewed items page must be displayed on the user account bar. Default value is true.
     */
    private boolean displayViewedItems = true;


    public ImageLink getHeader() {
        return header;
    }

    public void setHeader(final ImageLink header) {
        this.header = header;
    }

    public ImageLink getTravelHeader() {
        return travelHeader;
    }

    public void setTravelHeader(final ImageLink travelHeader) {
        this.travelHeader = travelHeader;
    }

    public List<NavigationLink> getVarMenuLinks() {
        return varMenuLinks;
    }

    public void setVarMenuLinks(final List<NavigationLink> varMenuLinks) {
        this.varMenuLinks = varMenuLinks;
    }

    public List<NavigationLink> getCustomStoreLinks() {
        return customStoreLinks;
    }

    public void setCustomStoreLinks(final List<NavigationLink> customStoreLinks) {
        this.customStoreLinks = customStoreLinks;
    }

    public boolean isHomeLandingEnabled() {
        return homeLandingEnabled;
    }

    public void setHomeLandingEnabled(final boolean homeLandingEnabled) {
        this.homeLandingEnabled = homeLandingEnabled;
    }

    public boolean isDisplayViewedItems() {
        return displayViewedItems;
    }

    public void setDisplayViewedItems(final boolean displayViewedItems) {
        this.displayViewedItems = displayViewedItems;
    }
}