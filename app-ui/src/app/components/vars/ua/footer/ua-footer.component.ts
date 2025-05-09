import { Component, HostListener, Inject, Input, OnInit } from '@angular/core';
import { Footer } from '@app/modules/footer/footer.component';
import { FooterDisclaimerService } from '@app/modules/footer/footer-disclaimer.service';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { DOCUMENT } from '@angular/common';
import { UserStoreService } from '@app/state/user-store.service';
import { NavigationEnd, Router, RouterEvent } from '@angular/router';
import { User } from '@app/models/user';

export interface BKTAG {
  bk_use_multiple_iframes: boolean;
  bk_allow_multiple_calls: boolean;
  // eslint-disable-next-line @typescript-eslint/naming-convention
  _reset: () => void;
  addPageCtx: (arg0: string, arg1: string) => void;
  doJSTag: (arg0: number, arg1: number) => void;
  addHash: (arg0: string, arg1: string, arg2: object) => void;
  util: Util;
}

export interface Util {
  normalizeEmail: (arg0: object) => void;
  normalizePhone: (arg0: object) => void;
}

@Component({
  selector: 'app-ua-footer',
  templateUrl: './ua-footer.component.html',
  styleUrls: ['./ua-footer.component.scss']
})
export class UAFooterComponent implements OnInit, Footer {

  @Input() messages: Messages;
  @Input() config: Config;
  @Input() footerData: object;
  @Input() scrollToTop: () => boolean;
  user: User;

  constructor(
    public footerDisclaimerService: FooterDisclaimerService,
    private userStoreService: UserStoreService,
    private router: Router,
    @Inject(DOCUMENT) private document: Document
  ) {
    this.user = this.userStoreService.user;
    this.loadCoreTagScript().onload = () => {
      this.onLoad();
      this.router.events.subscribe(
        (event: RouterEvent) => {
          if (event instanceof NavigationEnd) {
            this.onPathChange();
          }
        });
    };
  }

  ngOnInit(): void {}

  loadCoreTagScript(): HTMLScriptElement {
    const body = this.document.getElementsByTagName('body')[0];
    const scriptElememt = this.document.createElement('script');
    scriptElememt.src = '//tags.bkrtx.com/js/bk-coretag.js';
    scriptElememt.type = 'text/javascript';
    body.appendChild(scriptElememt);
    return scriptElememt;
  }

  // window.onLoad logic
  onLoad(): void {
    const pathname = window.location.pathname;
    // -- Rapid Retargeter Page Tracking
    if (pathname.indexOf('card_checkout') > -1) {
      this.invokeRREvent('UPDATE', 'Billing Information');
    }
    // -- Rapid Retargeter Visitor Tracking
    if (this.user) {
      const uEmail = this.user.email;
      const uPhone = this.user.phone;
      this.setRRVisitor(uEmail, uPhone);
    }
  }

  // window.pathChange logic
  onPathChange() {
    const pathname = window.location.pathname;
    // -- Fraud Check
    if (pathname.indexOf('checkout') > -1) {
      let htmUrl = null;
      if (window.location.port !== null || window.location.port !== '') {
        htmUrl = window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '/apple-gr/service/fraudcheck/redirectlogohtm';
      } else {
        htmUrl = window.location.protocol + '//' + window.location.hostname + '/apple-gr/service/fraudcheck/redirectlogohtm';
      }
      const iframe = document.getElementById('uaiframe');
      iframe.setAttribute('src', htmUrl);
    }
    // -- Rapid Retargeter Page Tracking
    if (['cart', 'checkout', 'confirmation'].indexOf(pathname) > -1) {
      const pageName = (pathname.indexOf('cart') > -1) ? 'Cart' : 'Checkout/Review';
      this.invokeRREvent('UPDATE', pageName);
    }
  }

  // RR Analytics Event Tracking
  @HostListener('document:click', ['$event'])
  purchaseWithMiles(event: MouseEvent) {

    if ( event.target && (event.target as Element).id ) {
      let type = (event.target as Element).id;

      if ( type !== 'btn-addtocart' && type !== 'btn-complete-purchase' &&
           type !== 'cApprove' && type.indexOf('btn-remove-item') === -1 ) {
        return;
      }

      if (type.indexOf('btn-remove-item') > -1) {
        type = 'btn-remove-item';
      }

      event.stopImmediatePropagation();
      switch (type) {
        case 'btn-addtocart':
          // Add Item to Cart
          this.invokeRREvent('ADD');
          break;
        case 'btn-remove-item':
          // Remove Item from Cart
          const idStr = (event.target as Element).id;
          const idArr = idStr.split('-');
          const sku = idArr[3].slice(5);
          this.invokeRREvent('REMOVE', sku);
          break;
        case 'cApprove':
          // Purchase Product(s) with Miles & Credit Card
          this.invokeRREvent('PURCHASE');
          break;
        case 'btn-complete-purchase':
          // Purchase Product(s) with Miles
          this.invokeRREvent('PURCHASE');
          break;
        default:
          break;
      }
    }
  }

  setRRVisitor(email, phone) {
    if (email || phone) {
      window.BKTAG.util.normalizeEmail(email);
      window.BKTAG.util.normalizePhone(phone);
      window.BKTAG.addHash('e_id_m36540', 'e_id_s36540', email);
      window.BKTAG.addHash('p_id_m36540', 'p_id_s36540', phone);
      // pass your site ID and the pixel limit:
      window.BKTAG.doJSTag(36540, 1);
    }
  }

  invokeRREvent(action, param?) {
    window.BKTAG.bk_use_multiple_iframes = true;
    window.BKTAG.bk_allow_multiple_calls = true;
    window.BKTAG.addPageCtx('Aid', '5765');
    window.BKTAG.addPageCtx('Etype', 'CART');
    window.BKTAG.addPageCtx('Pid', 'Apple');
    window.BKTAG.addPageCtx('Action', action);
    if (action === 'UPDATE') {
      window.BKTAG.addPageCtx('Stage', param);
    }
    window.BKTAG.doJSTag(54167, 1);
    window.BKTAG._reset();
  }

}
