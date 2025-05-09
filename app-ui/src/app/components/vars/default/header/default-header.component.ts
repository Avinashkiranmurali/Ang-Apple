import { Component, Input, OnInit } from '@angular/core';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';
import { Program } from '@app/models/program';
import { Messages } from '@app/models/messages';

@Component({
  selector: 'app-default-header',
  templateUrl: './default-header.component.html',
  styleUrls: ['./default-header.component.scss']
})
export class DefaultHeaderComponent implements OnInit {

  @Input() headerTemplate: object;
  @Input() messages: Messages;
  @Input() config: Config;
  @Input() program: Program;
  @Input() user: User;

  constructor() {}

  ngOnInit(): void {
    if (this.getSuppressLogoTemplate()) {
      this.config['clientHeaderBackgroundColor'] = '#fff';
    }
  }

  getSuppressLogoTemplate(): boolean {
    if (this.headerTemplate['suppressLogoTemplate']  && this.headerTemplate['suppressLogoTemplate'] ==='Y') {
      return true;
    }
    return  false;
  }

}
