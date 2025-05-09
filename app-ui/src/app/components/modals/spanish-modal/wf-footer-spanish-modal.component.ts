import { Component, OnInit, Input } from '@angular/core';
import {MessagesStoreService} from '@app/state/messages-store.service';
import {Messages} from '@app/models/messages';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  templateUrl: './wf-footer-spanish-modal.component.html',
  styleUrls: ['./wf-footer-spanish-modal.component.scss']
})
export class WFSpanishComponent implements OnInit {
  messages: Messages;
  constructor(
    public messageStore: MessagesStoreService,
    public activeModal: NgbActiveModal
  ) {
    this.messages = this.messageStore.messages;
  }

  ngOnInit(): void {}

  cancel() {
    this.activeModal.dismiss();
  }

}
