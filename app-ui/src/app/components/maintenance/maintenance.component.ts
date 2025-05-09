import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PublicMessagesService } from '@app/services/public-messages.service';
import { Subscription } from 'rxjs';
@Component({
  selector: 'app-maintenance',
  templateUrl: './maintenance.component.html',
  styleUrls: ['./maintenance.component.scss']
})
export class MaintenanceComponent implements OnInit, OnDestroy {

  locale: string;
  publicMessages: { [key: string]: any};
  private subscriptions: Subscription[] = [];

  constructor(
    private activatedRoute: ActivatedRoute,
    private publicMessagesService: PublicMessagesService
  ) { }

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      this.locale = params['locale'];
      this.getPublicMessages();
    });
  }

  getPublicMessages() {
    this.subscriptions.push(
      this.publicMessagesService.getPublicMessages(this.locale).subscribe(data => {
        this.publicMessages = data;
        this.publicMessages.maintenanceMessage = data.maintenanceMessage ? this.decodeHTMLEntities(data.maintenanceMessage) : data.maintenanceMessage;
      })
    );
  }

  decodeHTMLEntities(message) {
    const doc = new DOMParser().parseFromString(message, 'text/html');
    return doc.documentElement.textContent;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
