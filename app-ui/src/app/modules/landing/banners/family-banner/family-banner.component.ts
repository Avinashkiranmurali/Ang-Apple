import { Component, Input, OnInit } from '@angular/core';
import { Messages } from '@app/models/messages';

@Component({
  selector: 'app-family-banner',
  templateUrl: './family-banner.component.html',
  styleUrls: ['./family-banner.component.scss']
})

export class FamilyBannerComponent implements OnInit {

  @Input() bannerData: object;
  @Input() messages: Messages;
  @Input() familyBanner: Array<any>;
  @Input() imageServerUrl: string;
  @Input() getBannerClass: (cat, slug) => boolean;
  @Input() category: string;
  @Input() familyBannerOnMobile: object;
  @Input() buildBannerLink: (cat) => string ;
  constructor() {
  }

  ngOnInit(): void {
  }

  preventChildClick(event: Event) {
    event.stopPropagation();
  }

  displayBanner(banerObj) {
    return (this.messages[banerObj.active] !== undefined || this.messages[banerObj.active] !== 'true');
  }
}
