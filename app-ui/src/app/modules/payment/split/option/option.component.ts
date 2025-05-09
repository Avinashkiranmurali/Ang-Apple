import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { TitleCasePipe } from '@angular/common';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { User } from '@app/models/user';
import { Program } from '@app/models/program';
import { Config } from '@app/models/config';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-split-option',
  templateUrl: './option.component.html',
  styleUrls: ['./option.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SplitOptionComponent implements OnDestroy {

  messages;
  user: User;
  state;
  program: Program;
  config: Config;
  pointLabel: string;
  translateParams: { [key: string]: string };
  translateParamsWithTitleCase: { [key: string]: string };

  private subscriptions: Subscription[] = [];

  constructor(
    public messageStore: MessagesStoreService,
    public stateService: PaymentStoreService,
    public userStore: UserStoreService,
    private translateService: TranslateService,
    private titleCasePipe: TitleCasePipe,
    private sharedService: SharedService
  ) {
    this.messages = this.messageStore.messages;
    this.user = this.userStore.user;
    this.program = this.userStore.program;
    this.config = this.userStore.config;

    if (this.program.formatPointName !== '') {
      this.pointLabel = this.translateService.instant(this.program.formatPointName);
      this.translateParams = {
        pointLabel: this.pointLabel
      };
      this.translateParamsWithTitleCase = {
        pointLabel: this.titleCasePipe.transform(this.pointLabel)
      };
    }

    this.subscriptions.push(
      this.stateService.get().subscribe(data => {
        this.state = data;
        this.translateParams = this.sharedService.getTranslateParams(this.translateParams, null, this.state).params;
      })
    );
  }

  changeSplitPayOption() {
    this.state = this.sharedService.updateSplitPayOption(this.state);
    this.state.selections.payment.paySummaryTemplate = this.state.selections.payment?.splitPayOption.paySummaryTemplate;
    this.stateService.set(this.state);
  }

  // TO convert object of object to array of objects
  getsplitPayOptionsJson() {
    if (this.state && this.state['selections'] && this.state['selections']['payment']) {
      const splitPayOptionsData = this.state['selections']['payment']['splitPayOptions'];
      // Step 1. Get all the object keys.
      const splitPayOptionsProperties = Object.keys(splitPayOptionsData);

      // Step 2. Create an empty array.
      const splitPayOptionsJson = [];

      // Step 3. Iterate throw all keys.
      let i = 0;
      for (const prop of splitPayOptionsProperties ) {
        splitPayOptionsJson.push(splitPayOptionsData[prop]);
        splitPayOptionsJson[i]['pptName'] = prop;
        i++;
      }
      return splitPayOptionsJson;
    }
    return [];
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
