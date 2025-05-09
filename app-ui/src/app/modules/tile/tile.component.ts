import { Component, Input, OnInit, ViewEncapsulation } from '@angular/core';
import { TemplateService } from '@app/services/template.service';
import { User } from '@app/models/user';
import { UserStoreService } from '@app/state/user-store.service';
import { Messages } from '@app/models/messages';
import { Product } from '@app/models/product';
import { SharedService } from '@app/modules/shared/shared.service';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { ActivatedRoute, Router } from '@angular/router';
import { AppConstants } from '@app/constants/app.constants';
import { SmartPrice } from '@app/models/smart-price';

@Component({
  selector: 'app-tile',
  templateUrl: './tile.component.html',
  styleUrls: ['./tile.component.scss'],
  encapsulation: ViewEncapsulation.None
})

export class TileComponent implements OnInit {
  user: User;
  payFrequency: string;
  @Input() item: Product;
  @Input() messages: Messages;
  @Input() imageSize: string;
  @Input() index: number;
  @Input() isSearchPage: boolean;
  routeParams: object;
  smartPrice: SmartPrice;

  constructor(
    private templateService: TemplateService,
    public userStore: UserStoreService,
    private sharedService: SharedService,
    private parsePsidPipe: ParsePsidPipe,
    private router: Router,
    private activatedRoute: ActivatedRoute
  ) {
    this.user = this.userStore.user;
    this.routeParams = {};
    this.activatedRoute.params.subscribe((params) => {
      this.routeParams['addCat'] = params['addCat'];
    });
  }

  ngOnInit(): void {
    if (this.user['program']['config']['payFrequency'] !== ''){
      this.payFrequency = this.user['program']['config']['payFrequency'];
    } else {
      this.payFrequency = '';
    }
    this.smartPrice = this.item.smartPrice;
  }

  setDetailsname(name) {
    this.userStore.detailsname = name;
  }

  constructRouterUrl(item, prev) {
    const itemPsid = this.psidSlugConvert(item.psid);
    const frameParam = this.router.url.indexOf('webshop') > -1;

    if (prev === 'cart' || frameParam || this.isSearchPage) {
      const temptype = item.categories[0].templateType;
      const itemAddcat = (item.categories[0].parents[0].parents.length > 0) ? item.categories[0].slug : '';
      const itemSubcat = (itemAddcat === '') ? item.categories[0].slug : item.categories[0].parents[0].slug;
      const itemCat = (itemAddcat === '') ? item.categories[0].parents[0].slug : item.categories[0].parents[0].parents[0].slug;
      if (temptype === 'LIST/GRID') {
        return ['/store/browse', itemCat, itemSubcat, itemPsid];
      } else if (temptype === 'CONFIGURABLE') {
        return ['/store/configure', itemCat, itemSubcat, itemPsid];
      } else if (itemAddcat !== '') {
        return ['/store/curated', itemCat, itemSubcat, itemAddcat, itemPsid];
      }
    } else {
      if (this.router.url.indexOf(AppConstants.CURATED) !== -1) {
        if (this.router.url.indexOf(this.routeParams['addCat']) === -1) {
          return item.categories[0].slug + '/' + itemPsid;
        } else {
          return itemPsid;
        }
      } else {
        return itemPsid;
      }
    }
  }

  psidSlugConvert(psidSlug) {
    return this.parsePsidPipe.transform(psidSlug, '-');
  }

  showMoreOptionsAvailable(item){
    // hide without any data
    if (item.optionsConfigurationData === null){
      return false;
    }

    const obj = Object.keys(item.optionsConfigurationData);

    // only one obj is available but that is not color so we have to show
    // || more obj is available so we have to show
    if ((obj.indexOf('color') === -1 && obj.length === 1) ||  (obj.indexOf('color') !== -1 && obj.length > 1 ) ) {
      return true;
    }

    // only one obj is available but that is color so we have to hide
    if (obj.indexOf('color') !== -1 && obj.length === 1 ){
      return false;
    }
  }

  showGridOptions(item) {
    const prodConfig = this.productConfigurationData();

    if (prodConfig) {
      return this.sharedService.getOptionsDisplay(prodConfig, item?.categories[0]?.parents[0]?.slug, item.categories[0]?.slug);
    } else {
      return false;
    }
  }

  productConfigurationData() {
    return this.templateService.getProperty('productConfiguration');
  }

}
