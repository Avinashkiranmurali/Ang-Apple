import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { NavStoreService } from '@app/state/nav-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Config } from '@app/models/config';
import { NgbPopover } from '@ng-bootstrap/ng-bootstrap';
import { Category } from '@app/models/category';
import { User } from '@app/models/user';
import { Messages } from '@app/models/messages';
import { MessagesStoreService } from '@app/state/messages-store.service';

@Component({
  selector: 'app-main-nav',
  templateUrl: './main-nav.component.html',
  styleUrls: ['./main-nav.component.scss']
})
export class MainNavComponent implements OnInit {
  @Output() public changeSubNav = new EventEmitter<any>();
  @Input() mobileNav: NgbPopover;
  mainNav: Array<Category>;
  messages: Messages;
  config: Config;
  user: User;
  constructor(
    private messageStore: MessagesStoreService,
    public mainNavStore: NavStoreService,
    public userStore: UserStoreService
  ) {}

  ngOnInit(): void {
    this.messages = this.messageStore.messages;
    this.config = this.userStore.config;
    this.mainNav = this.mainNavStore.mainNav;
    this.user = this.userStore.user;
  }

  changeSubnav(i): void {
    // this.subNav = this.mainNav[i].subCategories;
    this.changeSubNav.emit(i);
    this.mobileNav.close();
  }

}
