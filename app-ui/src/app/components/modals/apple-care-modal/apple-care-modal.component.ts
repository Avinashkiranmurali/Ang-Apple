import { Component, Input, OnInit } from '@angular/core';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
import { User } from '@app/models/user';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-apple-care-modal',
  templateUrl: './apple-care-modal.component.html',
  styleUrls: ['./apple-care-modal.component.scss']
})
export class AppleCareModalComponent implements OnInit {

  @Input() messages: Messages;
  @Input() config: Config;
  @Input() user: User;
  @Input() appleCareService: object; // EITHER PRODUCT OR CARTITEM
  isProcessing = false;

  constructor(
    private activeModal: NgbActiveModal,
  ) { }

  ngOnInit(): void { }

  cancel() {
    this.activeModal.close();
    return false;
  }

  doAppleCareSubscription() {
    this.activeModal.close(this.appleCareService);
  }

}
