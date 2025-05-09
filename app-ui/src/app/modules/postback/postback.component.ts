import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { CartService } from '@app/services/cart.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Config } from '@app/models/config';
import { PostBackOrderConformaction, PurchaseSelectionInfo } from '@app/models/postback-order-conformaction';
import { DomSanitizer } from '@angular/platform-browser';
import { SessionService } from '@app/services/session.service';
import { FormGroup, FormControl } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { OrderInformationService } from '@app/services/order-information.service';
import { TransitionService } from '@app/transition/transition.service';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-postback',
  templateUrl: './postback.component.html',
  styleUrls: ['./postback.component.scss']
})

export class PostbackComponent implements OnInit {
  url: string;
  config: Config;
  pbFormData: object = {};
  constructor(
    private cartService: CartService,
    public userStore: UserStoreService,
    private router: Router,
    private sanitizer: DomSanitizer,
    private sessionService: SessionService,
    private http: HttpClient,
    private orderInformationService: OrderInformationService,
    public sharedService: SharedService,
    private transitionService: TransitionService
  ) {
    this.config = this.userStore.config;
  }

  ngOnInit(): void {
    this.postBackControl();
  }


  postBackControl() {

    this.orderInformationService.getOrderInformation().subscribe((data: PostBackOrderConformaction) => {
      if (data != null && data.b2sOrderId != null) { // data.data != null    // DOTO
        const b2sOrderId = data.b2sOrderId;
        this.postInfo(b2sOrderId);
      }
    }, (error) => {
      this.transitionService.closeTransition();
      this.router.navigate(['store/checkout']);
    });
  }


  postInfo(orderId: number) {
    this.orderInformationService.getPurchaseSelectionInfo(orderId).subscribe(
      (data: PurchaseSelectionInfo) => {
        let url: string;
        this.pbFormData = {};
        const postData = data;
        if (postData != null && postData.purchasePostUrl != null) {
          url = postData.purchasePostUrl;
          const payrollRedirect = postData.payrollProviderRedirect;
          // we don't need to send purchasePostUrl to vitality
          if (this.config.postBackType === 'api' && !payrollRedirect) {
            window.location.href = url;
          } else {
            delete postData.purchasePostUrl;
            this.pbFormData['url'] = url;
            this.pbFormData['method'] = postData.method;
            delete postData.method;
            if (postData.jwt) {
              this.pbFormData['params'] = {};
              this.pbFormData['params']['jwt'] = postData.jwt;
            } else {
              const re = new RegExp(String.fromCharCode(160), 'g');
              const encode = postData['itemDescription'].replace(re, ' ');
              postData['itemDescription'] = encode;
              this.pbFormData['params'] = postData;
            }
            setTimeout(() => {
              this.transitionService.closeTransition();
              document.querySelector('form').submit();
            }, 1500);

          }
        }
      }, (error) => {
      });
  }

}
