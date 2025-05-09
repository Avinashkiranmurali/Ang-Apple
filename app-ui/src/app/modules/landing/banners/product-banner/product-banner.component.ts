import { Component, Input, OnInit } from '@angular/core';
import { Messages } from '@app/models/messages';

@Component({
  selector: 'app-product-banner',
  templateUrl: './product-banner.component.html',
  styleUrls: ['./product-banner.component.scss']
})

export class ProductBannerComponent implements OnInit {
  @Input() messages: Messages;
  @Input() product: string;
  @Input() productBanner: object;
  @Input() imageServerUrl: string;
  constructor() { }

  ngOnInit(): void {
  }

}
