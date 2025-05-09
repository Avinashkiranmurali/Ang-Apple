import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { Router } from '@angular/router';
import { Messages } from '@app/models/messages';

@Component({
  selector: 'app-consent-form',
  templateUrl: './consent-form.component.html',
  styleUrls: ['./consent-form.component.scss'],
})
export class ConsentFormComponent implements OnInit {
  @Input() messages: Messages;

  constructor(
    private activeModal: NgbActiveModal,
    private router: Router,
    public messageStore: MessagesStoreService
  ) {
    this.messages = this.messageStore.messages;
  }

  ngOnInit(): void {}

  agree(): boolean {
    this.activeModal.close();
    this.router.navigate(['/store/payment', 'split']);
    return false;
  }

  cancel(): boolean {
    this.activeModal.close();
    return false;
  }
}
