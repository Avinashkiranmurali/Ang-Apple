import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ActivatedRoute } from '@angular/router';
import { PublicMessagesService } from '@app/services/public-messages.service';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.scss']
})
export class ErrorComponent implements OnInit, OnDestroy {

  locale: string;
  publicMessages: { [key: string]: any};
  private subscriptions: Subscription[] = [];

  constructor(
    private activatedRoute: ActivatedRoute,
    private publicMessagesService: PublicMessagesService,
    private titleService: Title
  ) { }

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      this.locale = params['locale'];
      this.getPublicMessages();
      this.titleService.setTitle(params.pageName);
    });
  }

  getPublicMessages() {
    this.subscriptions.push(
      this.publicMessagesService.getPublicMessages(this.locale, 'login').subscribe(data => {
        this.publicMessages = data;
        this.publicMessages.offerNoLongerValid = data.offerNoLongerValid ? this.decodeHTMLEntities(data.offerNoLongerValid) : data.offerNoLongerValid;
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
