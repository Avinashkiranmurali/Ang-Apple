import { Component, Input, OnInit } from '@angular/core';
import { SharedService } from '@app/modules/shared/shared.service';
import { Config } from '@app/models/config';
import { Program } from '@app/models/program';

@Component({
  selector: 'app-logo',
  templateUrl: './logo.component.html',
  styleUrls: ['./logo.component.scss']
})
export class LogoComponent implements OnInit {

  @Input() headerTemplate: object;
  @Input() config: Config;
  @Input() program: Program;
  imgSrc: string;
  secImgSrc: string;

  constructor(
    public sharedService: SharedService
  ) { }

  ngOnInit(): void {
    this.imgSrc = this.getImgSrc();
    this.secImgSrc = this.getSecImgSrc();
  }

  navigateToClient(): boolean {
    this.sharedService.sessionTypeAction('navigateBack');
    return false;
  }

  navigateToStore(): boolean {
    this.sharedService.sessionTypeAction('navigateToStore');
    return false;
  }

  getImgSrc(): string {
    const imgSrc = this.program.imageUrl ? this.config.imageServerUrl + '/' + this.program.imageUrl : '';

    if (this.program.imageUrl) {
      return this.program.imageUrl.indexOf('http') === 0 ? this.program.imageUrl : imgSrc;
    }

    return imgSrc;
  }

  getSecImgSrc(): string {
    return this.program.config.secondaryImageUrl;
  }

  checkNavBack(): boolean {
    const storedUrls = sessionStorage.getItem('sessionURLs');
    const varUrls = JSON.parse(storedUrls);
    return varUrls?.navigateBackUrl;
  }

}
