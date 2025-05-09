import { Component, OnInit } from '@angular/core';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';

@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.scss']
})

export class PaymentComponent implements OnInit {

  constructor(private ensightenService: EnsightenService) { }

  ngOnInit(): void {
    // Analytics object
    const userAnalyticsObj = {
      pgName: '',
      pgType: '',
      pgSectionType: ''
    };
    this.ensightenService.broadcastEvent(userAnalyticsObj, []);
  }

}
