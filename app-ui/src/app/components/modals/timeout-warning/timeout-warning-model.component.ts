import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SessionService } from '@app/services/session.service';

@Component({
  selector: 'app-timeout-warning-model',
  templateUrl: './timeout-warning-model.component.html',
  styleUrls: ['./timeout-warning-model.component.scss'],
})
export class TimeoutWarningModelComponent implements OnInit {
  modalTemplate: string;
  @Input() sessionTimeoutRemaining: number;
  constructor(private activeModal: NgbActiveModal ) { }

  ngOnInit(): void { }

  extendSession(): boolean {
    // TODO
    /* $rootScope.dblclick = false;
    angular.element('.modal-dialog').attr("aria-hidden", true);

    var wrapper = angular.element('.wrapper');
    if (wrapper && wrapper.length) wrapper[0].removeAttribute("tabindex");

    var skipNav = angular.element('#skipNavigation');
    skipNav.attr("aria-hidden", false);
    if (skipNav && skipNav.length) skipNav[0].removeAttribute("tabindex");

    sessionMgmt.getSession();
    $modalInstance.dismiss(); */

    this.activeModal.close(true);
    return false;
  }

  cancel(): boolean {
    this.activeModal.close();
    return false;
  }
}
