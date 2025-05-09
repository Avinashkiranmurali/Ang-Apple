import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { Config } from '@app/models/config';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';

@Component({
  selector: 'app-notification-banner',
  templateUrl: './notification-banner.component.html',
  styleUrls: ['./notification-banner.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})

export class NotificationBannerComponent implements OnInit {

  @Input() public isEnableCustomNotificationRibbon: boolean;
  @Input() public isEnableNotificationRibbon: boolean;
  @Input() public persistCustomNotificationRibbon: boolean;
  @Input() public isShowNotificationRibbon: boolean;
  @Input() public notificationRibbonMessage: string;
  @Input() public config: Config;

  constructor(private notificationRibbonService: NotificationRibbonService) {

    // Enable/disable error notification ribbon based on emitters from child pages
    notificationRibbonService.changeEmitted$.subscribe(
      dataArray => {
        const bool = dataArray[0];
        const msg = dataArray[1];

        if (bool === true) {
          this.isEnableNotificationRibbon = true;
        }

        this.isShowNotificationRibbon = bool;
        this.notificationRibbonMessage = msg;
      });
  }

  ngOnInit(): void {
  }

  onCustomNotificationRibbonClose(event) {
    event.preventDefault();
    this.isEnableCustomNotificationRibbon = false;
    this.notificationRibbonService.setCustomRibbonShow(false);
    this.notificationRibbonService.setCustomRibbonClosed(true);
  }

  onNotificationRibbonClose(event) {
    event.preventDefault();
    this.isEnableNotificationRibbon = false;
    this.notificationRibbonService.setNotificationRibbonShow(false);
  }

  persistNotificationRibbon() {
    sessionStorage.setItem('persistCustomNotificationRibbon', 'true');
    this.persistCustomNotificationRibbon = true;
  }
}
