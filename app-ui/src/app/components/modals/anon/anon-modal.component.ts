import { Component, OnInit, Input } from '@angular/core';
import { TemplateStoreService } from '@app/state/template-store.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SessionService } from '@app/services/session.service';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';

@Component({
  selector: 'app-modals',
  templateUrl: './anon-modal.component.html',
  styleUrls: ['./anon-modal.component.scss']
})
export class AnonModalComponent implements OnInit {

  modalTemplate: object;
  @Input() messages: Messages;
  @Input() config: Config;
  @Input() user: User;
  constructor(
    private templateStoreService: TemplateStoreService,
    private sharedService: SharedService,
    private activeModal: NgbActiveModal,
    private sessionService: SessionService
  ) {
  }

  ngOnInit(): void {
    this.modalTemplate = this.templateStoreService.anonymousModal;
  }

  doSignIn(): boolean {
    this.sharedService.sessionTypeAction('signIn');
    return false;
  }

  cancel(): boolean {
    this.sessionService.getSession();
    this.activeModal.close();
    return false;
  }

}
