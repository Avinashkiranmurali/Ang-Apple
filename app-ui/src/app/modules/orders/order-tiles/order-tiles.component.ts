import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { Config } from '@app/models/config';
import { Messages } from '@app/models/messages';
import { OrderDetail } from '@app/models/order-detail';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';

@Component({
  selector: 'app-order-tiles',
  templateUrl: './order-tiles.component.html',
  styleUrls: ['./order-tiles.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OrderTilesComponent implements OnInit {

  @Input() orderItems: Array<OrderDetail>;
  program: Program;
  user: User;
  messages: Messages;
  config: Config;
  pointLabel: string;
  locale: string;
  mediumDate: string;
  translateParams: { [key: string]: string };

  constructor(
    private messageStore: MessagesStoreService,
    private userStore: UserStoreService)
  {
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    this.messages = this.messageStore.messages;
    this.program = this.userStore.program;
    this.locale = this.user.locale.replace('_', '-');
    this.mediumDate = (this.config.mediumDate !== undefined) ? this.config.mediumDate : 'mediumDate';
  }

  ngOnInit(): void {
    this.pointLabel = this.messages[this.program.formatPointName];
    this.translateParams = {
      pointLabel: this.pointLabel
    };
  }

}
