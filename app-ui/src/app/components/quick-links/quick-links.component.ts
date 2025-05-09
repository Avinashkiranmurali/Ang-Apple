import { Component, EventEmitter, HostListener, Input, OnInit, Output } from '@angular/core';
import { QuickLink } from '@app/models/quick-link';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Router } from '@angular/router';
import { NgbPopover } from '@ng-bootstrap/ng-bootstrap/popover/popover';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';

@Component({
  selector: 'app-quick-links',
  templateUrl: './quick-links.component.html',
  styleUrls: ['./quick-links.component.scss']
})
export class QuickLinksComponent implements OnInit {

  messages: Messages;
  @Input() quickLinks: QuickLink[];
  @Input() mobileNav: NgbPopover;
  @Input() parentClass?: string;
  @Output() closeQuickLinks: EventEmitter<void> = new EventEmitter<void>();
  config: Config;


  constructor(
    public messageStore: MessagesStoreService,
    public userStore: UserStoreService,
    public router: Router
  ) {
    this.messages = messageStore.messages;
    // @ts-ignore
    this.config = this.userStore.config;
  }

  ngOnInit(): void { }
  @HostListener('click', ['$event'])
  onClick(event: MouseEvent) {
    // NgPopupClose event
    if (event.target instanceof HTMLAnchorElement === true){
      if (this.mobileNav) {
        this.mobileNav.close();
      } else {
        this.closeQuickLinks.emit();
      }
    }
    // If we don't have an anchor tag, we don't need to do anything.
    if (event.target instanceof HTMLAnchorElement === false || event.target['attributes']['data-external']) {
      return;
    }
    // Prevent page from reloading
    event.preventDefault();
    const target = event.target as HTMLAnchorElement;
    // Navigate to the path in the link
    this.router.navigate([target.pathname]);
  }

}
