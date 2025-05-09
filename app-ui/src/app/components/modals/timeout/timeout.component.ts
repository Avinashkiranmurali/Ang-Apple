import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { Messages } from '@app/models/messages';

@Component({
  selector: 'app-timeout',
  templateUrl: './timeout.component.html',
  styleUrls: ['./timeout.component.scss']
})
export class TimeoutComponent implements OnInit {

  @Input() messages: Messages;
  constructor(
    private activeModal: NgbActiveModal,
    private messageStore: MessagesStoreService,
    private sharedService: SharedService
  ) { }

  ngOnInit(): void {
  }
  cancel(): boolean {
    // TODO modal close and get the session
    this.activeModal.close();
    return false;
  }
  continue(): boolean {
    this.sharedService.sessionTypeAction('timeOut');
    this.activeModal.close();
    return false;
  }
}
