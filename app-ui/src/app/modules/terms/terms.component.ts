import { Component, OnInit } from '@angular/core';
import { TemplateStoreService } from '@app/state/template-store.service';
import { ViewportScroller } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { Messages } from '@app/models/messages';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';

@Component({
  selector: 'app-terms',
  templateUrl: './terms.component.html',
  styleUrls: ['./terms.component.scss']
})

export class TermsComponent implements OnInit {
  bodyTemplate: object;
  termsElements: Array<{[key: string]: string}>;
  translateParams: { [key: string]: string };
  messages: Messages;
  analyticsUserObject: any;

  constructor(
    private templateStoreService: TemplateStoreService,
    private activatedRoute: ActivatedRoute,
    private viewportScroller: ViewportScroller,
    private messageStore: MessagesStoreService,
    private sharedService: SharedService,
    private ensightenService: EnsightenService
  ) {
    this.messages = this.messageStore.messages;
    this.bodyTemplate = this.templateStoreService.bodyTemplate;
    this.termsElements = this.bodyTemplate['terms'] && this.bodyTemplate['terms']['elements'];
    this.analyticsUserObject = {};
    this.analyticsUserObject = this.sharedService.getAnalyticsUserObject(this.activatedRoute.snapshot.data);
    this.activatedRoute.fragment.subscribe((fragment: string) => {
      // if fragment exists, append fragment name with pgName
      let value = fragment;
      value = (value && value === 'SalesTandC') ? 'sales_terms' : value;
      let pageName = this.analyticsUserObject.pgName || '';
      pageName =  pageName && value ? pageName + ':' + value : pageName;

      const analyticsUserObj = {
        pgName: pageName.toLowerCase(),
        pgType: this.analyticsUserObject.pgType || '',
        pgSectionType: this.analyticsUserObject.pgSectionType || ''
      };
      this.ensightenService.broadcastEvent(analyticsUserObj, []);
      setTimeout(() => {
        this.viewportScroller.scrollToAnchor(fragment);
      });
    });

    this.translateParams = {
      StorePoliciesCustServiceNumber : this.messages.StorePoliciesCustServiceNumber
    };
  }

  ngOnInit(): void {
  }

}
