import {Component,ElementRef, EventEmitter, HostListener, Injectable, Input, OnInit, Output, SimpleChanges, ViewChild} from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { Router } from '@angular/router';
import { Messages } from '@app/models/messages';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-acknowledge-tc-modal',
  templateUrl: './acknowledge-tc-modal.component.html',
  styleUrls: ['./acknowledge-tc-modal.component.scss']
})
// @Injectable()
export class AcknowledgeTcModalComponent implements OnInit {

  static componentInstance: any;
  public disabledBtn: boolean;
  @Input() messages: Messages;
  @Output() passUserAction: EventEmitter<object> = new EventEmitter();
  @ViewChild('acknowledgeTcWrapper') modal!: ElementRef;
  @ViewChild('btnAgree') agreeButton!: ElementRef;

  modalContentFileName: string = 'modal-content.pdf';
  printElementSelector: string = 'acknowledge-tc-modal-body';

  private userAcknowledge: object;
  public isMobileModal: boolean;
  public translate: any;
  public tcContentScrolled: boolean = false;
  public enableUserNotScrolledMsg: boolean = false;

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    this.windowResize();
  }

  constructor(
    private activeModal: NgbActiveModal,
    private router: Router,
    public messageStore: MessagesStoreService
  ) {
    this.messages = this.messageStore.messages;
  }

  ngOnInit() {
    this.disabledBtn = false;
    const appWidth = window.innerWidth | document.documentElement.clientWidth;
    this.isMobileModal = (appWidth < 720);

  }

  windowResize() {
    const appWidth = window.innerWidth || document.documentElement.clientWidth;
    this.isMobileModal = (appWidth < 720);
  }

  checkScroll() {
    const modalBody = this.modal.nativeElement.querySelector('.acknowledge-tc-modal-body');
    const agreeButton = this.agreeButton ? this.agreeButton.nativeElement : '';
    if (agreeButton) {
      if (modalBody.scrollTop >= modalBody.scrollHeight - modalBody.offsetHeight - 10) {
        agreeButton.classList.remove('btn-disabled');
        agreeButton.removeAttribute('aria-disabled');
        this.tcContentScrolled = true;
        this.enableUserNotScrolledMsg = false;
      }
    }
  }

  close() {
    this.activeModal.close();
  }

  focusError(): void {
    setTimeout(() => {
      const errorMsgElement = document.querySelector('.error-text') as HTMLElement;
      if(errorMsgElement)
        errorMsgElement.focus();
    }, 0);
  }

  agree() {
    if (!this.tcContentScrolled && !this.isMobileModal) {
      this.enableUserNotScrolledMsg = true;
      this.focusError();
      return false;
    }
    this.userAcknowledge = {
      userAcknowledged: true
    }
    this.activeModal.close(this.userAcknowledge);
    return false;
  }

  decline() {
    if (this.tcContentScrolled || this.isMobileModal) {
      this.enableUserNotScrolledMsg = false;
      this.activeModal.close();
      this.userAcknowledge = {
        userAcknowledged: false
      }
      this.activeModal.close(this.userAcknowledge);
      return false;
    } else {
      this.enableUserNotScrolledMsg = true;
      this.focusError();
    }
  }
}
