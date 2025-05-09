import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Messages } from '@app/models/messages';
import { OrderDetail } from '@app/models/order-detail';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { SessionService } from '@app/services/session.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { Subscription } from 'rxjs';
import { OrdersService } from '@app/modules/orders/orders.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';

@Component({
  selector: 'app-orders',
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit, OnDestroy {

  messages: Messages;
  orders: Array<OrderDetail>;
  private subscriptions: Subscription[] = [];
  isLoading: boolean;

  constructor(
    private messageStore: MessagesStoreService,
    private ordersService: OrdersService,
    private ensightenService: EnsightenService,
    private activatedRoute: ActivatedRoute,
    private sessionService: SessionService,
    public sharedService: SharedService,
    private notificationRibbonService: NotificationRibbonService
  ) {
    this.messages = this.messageStore.messages;
  }

  ngOnInit(): void {
    this.isLoading = true;
    this.getOrders();
    const analyticsUserObject = this.sharedService.getAnalyticsUserObject(this.activatedRoute.snapshot.data);
    if (Object.keys(analyticsUserObject).length > 0 ) {
      this.ensightenService.broadcastEvent(analyticsUserObject, []);
    }
  }

  getOrders(): void {
    this.subscriptions.push(
      this.ordersService.getOrderHistory().subscribe(
      (data: Array<OrderDetail>) => {
        this.isLoading = false;
        this.orders = data;
      },
      error => {
        this.isLoading = false;
        if (error.status === 401 || error.status === 0) {
          this.sessionService.showTimeout();
        } else {
          // NOTIFICATION RIBBON ERROR MESSAGE
          this.notificationRibbonService.emitChange([true, this.messages.unknownError]);
        }
      }
    ));
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

}
