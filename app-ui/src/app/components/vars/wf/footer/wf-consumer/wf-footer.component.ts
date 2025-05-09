import { Component, OnInit, Input } from '@angular/core';
import { Footer } from '@app/modules/footer/footer.component';
import { FooterDisclaimerService } from '@app/modules/footer/footer-disclaimer.service';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { SharedService } from '@app/modules/shared/shared.service';
import {MessagesStoreService} from '@app/state/messages-store.service';
import {ModalsService} from '@app/components/modals/modals.service';

@Component({
  selector: 'app-wf-footer',
  templateUrl: './wf-footer.component.html',
  styleUrls: ['./wf-footer.component.scss']
})
export class WFFooterComponent implements OnInit, Footer {

  @Input() messages: Messages;
  @Input() config: Config;
  @Input() footerData: object;
  @Input() scrollToTop: () => boolean;

  constructor(
    public messageStore: MessagesStoreService,
    public footerDisclaimerService: FooterDisclaimerService,
    public sharedService: SharedService,
    public modalService: ModalsService
  ) {
    this.messages = this.messageStore.messages;
  }

  ngOnInit(): void {
  }

  getSpanishModal(event) {
    event.preventDefault();
    this.modalService.openWfSpanishComponent();
  }

  signOutAction(action): boolean {
    this.sharedService.sessionTypeAction(action);
    return false;
  }

}
