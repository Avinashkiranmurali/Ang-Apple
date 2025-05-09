import { Component, Input, OnInit } from '@angular/core';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';
import { Price } from '@app/models/price';

@Component({
  selector: 'app-display-price',
  templateUrl: './display-price.component.html',
  styleUrls: ['./display-price.component.scss']
})
export class DisplayPriceComponent implements OnInit {

  messages: Messages;
  config: Config;
  program: Program;
  user: User;
  pricingTemplate: string;
  @Input() totalVal: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  pointLabel: string;
  isPoints: boolean;
  @Input() ext: string;
  @Input() showTaxDisclaimer;
  @Input() displayCurrencyPrice = true;
  @Input() displayPointsPrice = true;

  constructor(
    public messageStore: MessagesStoreService,
    private userStore: UserStoreService
  ) {
    this.messages = messageStore.messages;
    this.config = this.userStore.config;
    this.pricingTemplate = this.config.pricingTemplate;
    this.program = this.userStore.program;
    this.user = this.userStore.user;
    this.pointLabel = this.messages[this.program.formatPointName];
  }

  ngOnInit(): void {
  }

  getPricingTemplate(ctTempType) {
    const splitArr = ctTempType.split('_');
    this.isPoints = (splitArr[0] === 'points');

    // return 'total-cash_points' + '-template.htm'; For testing purpose......

    if (ctTempType === 'points_cash' || ctTempType === 'cash_points') {
      return ctTempType + '-template.htm';
    } else {
      return 'default-template.htm';
    }
  }
}
