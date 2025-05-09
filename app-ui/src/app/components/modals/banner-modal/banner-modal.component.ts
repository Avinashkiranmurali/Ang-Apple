import { Component, Input, ViewChild, ViewEncapsulation, Renderer2, AfterViewInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { Messages } from '@app/models/messages';

@Component({
  selector: 'app-banner-modal',
  templateUrl: './banner-modal.component.html',
  styleUrls: ['./banner-modal.component.scss'],
  encapsulation: ViewEncapsulation.None
})
// @Injectable()
export class BannerModalComponent implements AfterViewInit{

  static componentInstance: any;
  public disabledBtn: boolean;
  @Input() messages: Messages;
  @Input() public modalHtml: any;
  @Input() public myChasePlanLink: any;
  @Input() public categoryId?: string = '';
  @Input() public config: any;
  @Input() public imageServerUrl?: string;
  @ViewChild('bannerModal') bannerModal: any;

  constructor(
    private activeModal: NgbActiveModal,
    public messageStore: MessagesStoreService,
    private renderer: Renderer2,
  ) {
    this.messages = this.messageStore.messages;
  }

  ngAfterViewInit() {
    if (this.bannerModal) {
      const anchors = this.bannerModal.nativeElement.getElementsByTagName('a') as HTMLAnchorElement[];
      for (const anchor of anchors) {
        if (anchor.hasAttribute('id') && anchor.getAttribute('id')==='myChasePlanLink') {
          this.renderer.listen(anchor, 'click', event => {
            event.preventDefault();
            window.open(this.myChasePlanLink, '_blank');
          });
        }
      }
    }
  }

  close(event: Event) {
    event.preventDefault();
    this.activeModal.close();
  }

}
