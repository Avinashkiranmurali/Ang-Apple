import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-single-banner',
  templateUrl: './single-banner.component.html',
  styleUrls: ['./single-banner.component.scss']
})
export class SingleBannerComponent implements OnInit {

  @Input() banner?: any;
  @Input() isMobile?: boolean;
  @Input() imageServerUrl?: string;

  constructor() { }

  ngOnInit(): void {
  }

}
