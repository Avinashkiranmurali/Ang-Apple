import { Component, Input, OnInit } from '@angular/core';
import { UserStoreService } from '@app/state/user-store.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { CartItem } from '@app/models/cart';
import { Offer } from '@app/models/offer';
import { Program } from '@app/models/program';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
@Component({
  selector: 'app-discount-temp',
  templateUrl: './discount-temp.component.html',
  styleUrls: ['./discount-temp.component.scss']

})
export class DiscountTempComponent implements OnInit {

  messages: Messages;
  config: Config;
  program: Program;
  pointLabel: string;
  @Input() offers: Offer;
  @Input() item: CartItem;
  @Input() itemTotal: number;
  @Input() ext: string;
  @Input() parentClass?: string;

  constructor(
    public messageStore: MessagesStoreService,
    private userStore: UserStoreService
  ) {
    this.messages = messageStore.messages;
    this.config = this.userStore.config;
    this.program = this.userStore.program;
  }

  ngOnInit(): void {
      this['pointLabel'] = this.program['formatPointName'];
  }

}
