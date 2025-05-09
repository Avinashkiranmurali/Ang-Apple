import { Component, OnInit, Input } from '@angular/core';
import { Footer } from '@app/modules/footer/footer.component';
import { FooterDisclaimerService } from '@app/modules/footer/footer-disclaimer.service';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';

@Component({
  selector: 'app-chase-footer',
  templateUrl: './chase-footer.component.html',
  styleUrls: ['./chase-footer.component.scss']
})
export class ChaseFooterComponent implements OnInit, Footer {

  @Input() messages: Messages;
  @Input() config: Config;
  @Input() footerData: object;
  @Input() scrollToTop: () => boolean;
  baseUrl: string;
  displayDynamicHeaderFooter: boolean;

  constructor(
    public footerDisclaimerService: FooterDisclaimerService
  ) {}

  ngOnInit(): void {
    this.baseUrl = this.config.externalHeaderUrl ? this.config.externalHeaderUrl : '';
    this.displayDynamicHeaderFooter = this.config.dynamicHeaderFooterLoad;
  }

}
