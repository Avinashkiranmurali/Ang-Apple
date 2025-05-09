import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';

import { Config } from '@app/models/config';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { UserStoreService } from '@app/state/user-store.service';

@Injectable({
  providedIn: 'root'
})
export class GridEventHandlerService {

  config: Config;

  constructor(
    @Inject(DOCUMENT) public document: Document,
    private parsePsidPipe: ParsePsidPipe,
    private notificationRibbonService: NotificationRibbonService,
    public userStore: UserStoreService,
  ) {
    this.config = this.userStore.config;
  }

  getGrid() {
    const  grid = this.document.getElementById('grid');

    return  {
      offsetHeight : grid.offsetHeight,
      scrollHeight : grid.scrollHeight
    };
  }

  getFacetsContainerFullView() {
    const facet = this.document.getElementsByTagName('app-facets-filters-component')[0];

    if (!facet) {
      return '';
    }
    return  {
      offsetHeight : facet['offsetHeight']
    };
  }

  getFacetsContainerView() {
    const facet = this.document.getElementsByClassName('facets-filters-template-container')[0];
    if (!facet) {
      return '';
    }
    return  {
      offsetHeight : facet['offsetHeight']
    };
  }

  facetsScroll = (event, facetsScrollObj)  => {
    let isFacetExpand = false;

    if (!event) {
      isFacetExpand = true;
    }

    const documentScrollTop = this.document.documentElement.scrollTop;
    facetsScrollObj['scrollTopPrev'] = (facetsScrollObj.scrollTop) ? facetsScrollObj.scrollTop : 0;
    facetsScrollObj['scrollTop'] = documentScrollTop;

    // facets Reference
    const facetsContainer = this.document.getElementsByClassName('facets-filters-template-container')[0];

    if (!facetsContainer) {
      return facetsScrollObj;
    }

    const facetsContainerBelowSpace = 0  ;
    const facetsContainerFullView = this.getFacetsContainerFullView();
    const facetsContainerHeight = this.getFacetsContainerView()['offsetHeight'] + facetsContainerBelowSpace + 25;
    const productListContainer = this.document.getElementsByClassName('product-result-list')[0];
    const grid = this.getGrid();
    const scrollPosition = window.innerHeight + window.scrollY;
    const productListContainerBottomPosition = facetsScrollObj.scrollStarts + productListContainer['offsetHeight'];
    const parentControlTop = this.document.querySelector('.parent-container').getBoundingClientRect().top;
    const isScrollreachedFacets = ( (documentScrollTop > facetsScrollObj.scrollStarts) && (parentControlTop <= facetsScrollObj.filterBarTop ));
    const isScrollReachedBottom = (scrollPosition >= productListContainerBottomPosition) ;
    let transition = 0;

    // facet and list container
    if (facetsContainerFullView['offsetHeight'] > grid['offsetHeight']) {
        facetsScrollObj.endPositionFacetsContainer = false;
        facetsScrollObj.fixedFacetsContainer = false;
        return facetsScrollObj;
    }
    // Veirify - scroll reach the facets Top Position
    if (isScrollreachedFacets && !isScrollReachedBottom) {
      facetsScrollObj.fixedFacetsContainer = true;  // set the point-fixed class to facetsContainer element
      facetsScrollObj.endPositionFacetsContainer = false;

      if (isFacetExpand) {
        return facetsScrollObj;
      }

      // find transition
      if (facetsScrollObj.scrollTop > facetsScrollObj.scrollTopPrev) { // scroll down
        transition = facetsScrollObj.transition - (facetsScrollObj.scrollTop - facetsScrollObj.scrollTopPrev);
      } else if (facetsScrollObj.scrollTop < facetsScrollObj.scrollTopPrev) { // scroll up
        transition = facetsScrollObj.transition + (facetsScrollObj.scrollTopPrev - facetsScrollObj.scrollTop);
      }

      if (transition > 0) {
        transition = 0;
      }

      // finding max transition
      if (facetsContainerHeight  > window.innerHeight) {
        facetsScrollObj.maxTransition =  window.innerHeight - (facetsContainerHeight + facetsScrollObj.topFreezedElementHeight - 5)  - facetsContainerBelowSpace ; // adjust for top 80, 25 will set in the facets Height, 55 is set in the max transition calculation
      }

      if (transition > facetsScrollObj.maxTransition) {
        facetsScrollObj.transition = transition;
        facetsContainer.setAttribute('style', 'transform: translate3d(0px,' + transition  + 'px, 0px)'); // adjusting the bottom positioning.

      }
    } else {
      const topFreezedElementHeight = facetsScrollObj.topFreezedElementHeight ;
      let viewedFooterHeight = scrollPosition - grid.scrollHeight - facetsScrollObj.scrollStarts - topFreezedElementHeight;
      viewedFooterHeight = (viewedFooterHeight > 0) ? viewedFooterHeight : 0;

      let facetContainerSpace = 0;
      let facetsHasAvailableSpace = false;

      if (viewedFooterHeight > 0) {
        facetContainerSpace = window.innerHeight - topFreezedElementHeight - viewedFooterHeight;
        facetsHasAvailableSpace = facetsContainerHeight < facetContainerSpace;
      } else if (isScrollReachedBottom) {
        facetContainerSpace = window.innerHeight -  topFreezedElementHeight;
        facetsHasAvailableSpace = facetsContainerHeight < facetContainerSpace;
      }

      if (facetsHasAvailableSpace && isScrollreachedFacets) {
       facetsScrollObj.fixedFacetsContainer = true;
       facetsScrollObj.endPositionFacetsContainer = false;
       return facetsScrollObj;
      } else {
        facetsScrollObj.fixedFacetsContainer = false; // remove the point-fixed class from facetsContainer element
        facetsContainer.setAttribute('style', 'transform: translate3d(0px, 0px, 0px)');
        if (isScrollReachedBottom && isScrollreachedFacets) {
          facetsScrollObj.fixedFacetsContainer = false;
          facetsScrollObj.endPositionFacetsContainer = true; // remove the position-end class from
          // facetsContainer element
        }
        else {
          facetsScrollObj.fixedFacetsContainer = false;
          facetsScrollObj.endPositionFacetsContainer = false;
        }
      }
    }
    return facetsScrollObj;
  };

  filterStickyTopOffset(facetsScrollObj) {
    let filterBarTop: number;
    let facetsFixedContainerTop = 0;

    if ((this.notificationRibbonService.getCustomRibbonShow() && !this.notificationRibbonService.getCustomRibbonPersist()) || this.notificationRibbonService.getNotificationRibbonShow()) {
      // If one or both notification ribbons are present, then offset
      // Please note that they stack and cover each other, so offset is the same regardless of number of ribbons
      filterBarTop = document.querySelector('.nr-notificationbanner')['offsetHeight'] - 1 || 52; // ribbon height;
      facetsScrollObj.notificationRibbonEnabled = true;
      facetsScrollObj.topFreezedElementHeight = 112;
      facetsFixedContainerTop = 112;

      if (this.config.externalHeaderUrl) {
        filterBarTop = -1;
        facetsScrollObj.topFreezedElementHeight = 60;
        facetsFixedContainerTop = 60;
      }
    } else {
      // default sticky (no ribbons)
      filterBarTop = -1;
      facetsScrollObj.notificationRibbonEnabled = false;
      facetsScrollObj.topFreezedElementHeight = 60;
      facetsFixedContainerTop = 60;
    }

    if (this.config.externalHeaderUrl) {
      const freezedHeaderObj =  document.getElementById('header-fixed');
      let freezedHeaderHeight = 0;

      if (freezedHeaderObj) {
        freezedHeaderHeight = freezedHeaderObj['offsetHeight'] ;
      }

      facetsFixedContainerTop = facetsFixedContainerTop + freezedHeaderHeight ;
      facetsScrollObj.topFreezedElementHeight = facetsScrollObj.topFreezedElementHeight + freezedHeaderHeight;

      // header height may varied, we need to set dynamic top position
      if (document.getElementsByClassName('facets-filters-fixed-container')[0]) {
        facetsFixedContainerTop = facetsFixedContainerTop + 20;
        document.getElementsByClassName('facets-filters-fixed-container')[0]['style'].top = facetsFixedContainerTop + 'px';
      }
      filterBarTop = filterBarTop + freezedHeaderHeight;

    }
    facetsScrollObj.filterBarTop = filterBarTop;

    return facetsScrollObj;
  }

  cleanData(oldItems) {
    oldItems.map((item) => {
      if (item.psid) {
        // Create a valid routing slug from the unique PSID by removing characters
        item.psidSlug = this.parsePsidPipe.transform(item.psid, '-');
      }

      if (item.options) {
        const optOrder = ['storage', 'memory', 'processor', 'graphics'];

        item.options.map((opt) => {
          opt.orderBy = optOrder.indexOf(opt.name);
        });
      }
    });

    return oldItems;
  }
}
