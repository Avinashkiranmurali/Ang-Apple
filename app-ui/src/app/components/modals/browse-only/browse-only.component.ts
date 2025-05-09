import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { Messages } from '@app/models/messages';

@Component({
  selector: 'app-browse-only',
  templateUrl: './browse-only.component.html',
  styleUrls: ['./browse-only.component.scss']
})
export class BrowseOnlyComponent implements OnInit {

  @Input() messages: Messages;

  constructor(
    private activeModal: NgbActiveModal,
    private messageStore: MessagesStoreService
  ) {
    // this.messages = messageStore.messages;
  }

  ngOnInit(): void {
  }

  cancel(): boolean {
    // TODO modal close and get the session
    this.activeModal.close();
    return false;
  }

}
