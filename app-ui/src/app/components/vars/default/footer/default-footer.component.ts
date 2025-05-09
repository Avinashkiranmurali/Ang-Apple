import { Component, Input, Injector } from '@angular/core';
import { Footer } from '@app/modules/footer/footer.component';
import { FooterDisclaimerService } from '@app/modules/footer/footer-disclaimer.service';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { Five9SocialWidget } from '@app/services/five9.service';
import { BreakPoint } from '@app/components/utils/break-point';

@Component({
  selector: 'app-default-footer',
  templateUrl: './default-footer.component.html',
  styleUrls: ['./default-footer.component.scss']
})
export class DefaultFooterComponent extends BreakPoint implements Footer {

  @Input() messages: Messages;
  @Input() config: Config;
  @Input() footerData: object;
  @Input() scrollToTop: () => boolean;

  constructor(
    public footerDisclaimerService: FooterDisclaimerService,
    public injector: Injector
  ) {
    super(injector);
  }

  get five9SocialWidget(): Five9SocialWidget {
    return window.Five9SocialWidget;
  }

  openFive9Chat(): boolean {
    this.five9SocialWidget.maximizeChat(this.five9SocialWidget.options);
    return false;
  }
}
