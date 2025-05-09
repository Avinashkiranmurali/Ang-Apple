import { Component, Inject, Input, OnInit } from '@angular/core';
import { UserStoreService } from '@app/state/user-store.service';
import { Footer } from '@app/modules/footer/footer.component';
import { FooterDisclaimerService } from '@app/modules/footer/footer-disclaimer.service';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import isWebview from 'is-ua-webview';
import { DOCUMENT } from '@angular/common';

@Component({
  selector: 'app-rbc-footer',
  templateUrl: './rbc-footer.component.html',
  styleUrls: ['./rbc-footer.component.scss']
})
export class RBCFooterComponent implements OnInit, Footer {

  @Input() messages: Messages;
  @Input() config: Config;
  @Input() footerData: object;
  @Input() scrollToTop: () => boolean;

  constructor(
    public userStore: UserStoreService,
    public footerDisclaimerService: FooterDisclaimerService,
    @Inject(DOCUMENT) private document: Document
  ) {
    if (isWebview(navigator.userAgent)) {
      this.hideElements();
    }
    this.config = this.userStore.config;
  }

  ngOnInit(): void {
  }

  hideElements(): void {
    // RBC native app: Hide Logo, Footer, Cart-Dropdown Navigation Links
    const styleText = '.logo-container > *, .wrapper-footer, .action-item-list' +
      ' li.action-item-navigateBack,.action-item-list li.action-item-signOut { display: none !important; } .action-item-list' +
      ' li { border-bottom: none !important; }';
    const style = this.document.createElement('style');
    style.textContent = styleText;
    if (this.document.body.appendChild) {
      this.document.body.appendChild(style);
    }
  }
}
