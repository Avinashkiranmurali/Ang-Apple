import { Component, OnInit, Input } from '@angular/core';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';

@Component({
  selector: 'app-program-info-banner',
  templateUrl: './program-info-banner.component.html',
  styleUrls: ['./program-info-banner.component.scss']
})
export class ProgramInfoBannerComponent implements OnInit {
  messages: Messages;
  config: Config;
  @Input() bannerData: object;
  @Input() programBanner: object;
  @Input() category: string;

  constructor(
    public messageStore: MessagesStoreService,
    public userStore: UserStoreService
  ) {
    this.messages = messageStore.messages;
    this.config = this.userStore.config;
  }

  ngOnInit(): void {
  }
}
