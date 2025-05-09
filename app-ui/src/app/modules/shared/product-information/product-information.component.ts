import { Component, Input, OnDestroy, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { AdditionalInfo } from '@app/models/additional-info';
import { Product } from '@app/models/product';
import { Messages } from '@app/models/messages';
import { SharedService } from '@app/modules/shared/shared.service';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { NgbCollapse } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-product-information',
  templateUrl: './product-information.component.html',
  styleUrls: ['./product-information.component.scss'],
  encapsulation: ViewEncapsulation.None,
})

export class ProductInformationComponent implements OnInit, OnDestroy {

  subcatName: string;
  key: string;
  public additionalInfo: AdditionalInfo;
  public additionalInfo1: AdditionalInfo;
  infoAccordian: {[key: string]: boolean};
  @Input() messages: Messages;
  @Input() details: Product;
  @Input() configItemSku: string;
  @Input() configItemUpc: string;
  @Input() pageTitle: string;
  @Input() parentClass?: string;
  @ViewChild('prodInfoSection') prodInfoSection: NgbCollapse;

  private subscriptions: Subscription[] = [];

  constructor(
    public sharedService: SharedService,
    private activatedRoute: ActivatedRoute) { }

  ngOnInit(): void {
    this.initCollapseState();
    if (Object.keys(this.details).length === 0){
      this.details = JSON.parse(sessionStorage.getItem('detailDataInfo'));
    }
    this.subscriptions.push(
      this.activatedRoute.params.subscribe(params => {
        if (Object.keys(params).length > 0) {
          this.initCollapseState();
        }
      }));
  }

  initCollapseState() {
    this.infoAccordian = {
      prodInfo: true,
      prodComp: true
    };
  }

  validateAdditionInfoData(data) {
    let parseData;
    const addInfoObj = {};

    if (!data) {
      return '';
    }

    try {
      parseData = JSON.parse(data);
    } catch (error) {
      parseData = data;
    }

    const isArray = parseData && typeof parseData === 'object' && parseData.constructor === Array;
    const isObject = parseData && typeof parseData === 'object' && parseData.constructor === Object;
    addInfoObj['type'] = 'text';

    if (isArray){
      addInfoObj['type'] = 'array';
    } else if (isObject) {
      addInfoObj['type'] = 'object';
    }

    addInfoObj['data'] = parseData;
    this.additionalInfo1 = addInfoObj;
    // return addInfoObj;
  }

  objectKeys(obj) {
    return Object.keys(obj);
  }

  ngOnDestroy() {
    this.subscriptions.forEach((subscription) => subscription.unsubscribe());
  }

}
