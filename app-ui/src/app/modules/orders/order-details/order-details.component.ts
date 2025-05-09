import { Component, OnInit , OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { OrderDetail, LineItem } from '@app/models/order-detail';
import { Subscription } from 'rxjs';
import { OrdersService } from '@app/modules/orders/orders.service';
import { Offer } from '@app/models/offer';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { SessionService } from '@app/services/session.service';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { UserStoreService } from '@app/state/user-store.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { User } from '@app/models/user';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';

@Component({
  selector: 'app-order-details',
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.scss']
})
export class OrderDetailsComponent implements OnInit, OnDestroy {
  orderId: number;
  isLoaded = false;
  orderDetails: OrderDetail;
  lineItems: Array<LineItem>;
  messages: Messages;
  config: Config;
  user: User;
  locale: string;
  hideOrderSummary: boolean;
  hidePaymentSummary: boolean;
  hideRefundSummary: boolean;
  public subscriptions: Subscription[] = [];

  constructor(
    private activatedRoute: ActivatedRoute,
    private messageStore: MessagesStoreService,
    private ordersService: OrdersService,
    private sessionService: SessionService,
    private notificationRibbonService: NotificationRibbonService,
    private userStoreService: UserStoreService,
    private sharedService: SharedService,
    private ensightenService: EnsightenService
  ) {
    this.messages = this.messageStore.messages;
    this.user = this.userStoreService.user;
    this.config = this.userStoreService.config;
    this.locale = this.user.locale.replace('_', '-');
    this.hideOrderSummary = Boolean(this.config.hideOrderSummary);
    this.hidePaymentSummary = Boolean(this.config.hidePaymentSummary);
    this.hideRefundSummary = Boolean(this.config.hideRefundSummary);
  }

  ngOnInit(): void {
    this.activatedRoute.params.subscribe(
      params => {
        this.orderId = params['orderId'];
      }
    );
    this.getOrderDetails();
    const routeData = this.activatedRoute.snapshot.data;
    if ( routeData && routeData.analyticsObj ) {
      const userAnalyticsObj = {
        pgName: routeData.analyticsObj.pgName.replace('<orderId>', this.orderId) || '',
        pgType: routeData.analyticsObj.pgType || '',
        pgSectionType: routeData.analyticsObj.pgSectionType || ''
      };
      this.ensightenService.broadcastEvent(userAnalyticsObj);
    }
  }

  getOrderDetails() {
    this.subscriptions.push(
      this.ordersService.getOrderDetails(this.orderId).subscribe((data: OrderDetail) => {
          data.lineItems.forEach((item, index) => {
            data.lineItems[index].offer = this.mapLineItemOffer(item);
          });
          this.orderDetails = data;
          this.isLoaded = true;
          if (Boolean(this.config.isSignOutEnabledForAnon)) {
            this.sharedService.sessionTypeAction('signOut');
          }
        },
        error => {
          this.isLoaded = true;
          if (error.status === 401 || error.status === 0) {
            this.sessionService.showTimeout();
          } else {
            // NOTIFICATION RIBBON ERROR MESSAGE
            this.notificationRibbonService.emitChange([true, this.messages.unknownError]);
          }
        }
      ));
  }

  mapLineItemOffer(lineItem: LineItem) {
    return {
      displayPrice: {...lineItem.unitPrice},
      unpromotedDisplayPrice: lineItem.unpromotedUnitPrice ? {...lineItem.unpromotedUnitPrice} : null
    } as Offer;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
