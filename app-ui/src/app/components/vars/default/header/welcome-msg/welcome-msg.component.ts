import {AfterViewInit, Component, Input, OnInit} from '@angular/core';
import { SharedService } from '@app/modules/shared/shared.service';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';
import { Program } from '@app/models/program';
import { Messages } from '@app/models/messages';
import { UserStoreService } from '@app/state/user-store.service';


@Component({
  selector: 'app-welcome-msg',
  templateUrl: './welcome-msg.component.html',
  styleUrls: ['./welcome-msg.component.scss']
})
export class WelcomeMsgComponent implements OnInit, AfterViewInit {

  @Input() program: Program;
  @Input() user: User;
  @Input() messages: Messages;
  @Input() config: Config;
  @Input() headerTemplate: object;
  firstname: string;
  points: number;
  pointLabel: string;
  loginRequired: boolean;
  translateParams: { [key: string]: string };

  constructor(
    public sharedService: SharedService,
    public userStore: UserStoreService,
  ) {
  }

  ngOnInit(): void {
    const fullName = this.user.sessionUserInfo?.fullName || this.user.fullName;
    const firstName = this.user.sessionUserInfo?.firstName || this.user.firstName;
    const lastName = this.user.sessionUserInfo?.lastName || this.user.lastName;

    this.translateParams = {
      fullName: fullName || '',
      firstName: firstName || '',
      lastName: lastName || '',
      headerName: this.user.headerName || '',
    };
    this['pointLabel'] = this.messages[this.program.formatPointName];
    this.userStore.get().subscribe((data) => {
        this.points = data.balance;
      }
    );
    this.loginRequired = this.config['login_required'];
  }

  ngAfterViewInit(): void {
    this.adjustMargins();
  }

  doSignIn(): void {
    this.sharedService.sessionTypeAction('signIn');
  }

  doSignOut(): void {
    this.sharedService.sessionTypeAction('signOut');
  }

  adjustMargins(): void {
      if (this.headerTemplate['suppressLogoTemplate']  && this.headerTemplate['suppressLogoTemplate'] ==='Y') {
            document.getElementsByClassName(this.headerTemplate['userLineTemplate']['class'])[0].setAttribute('style','margin: 1em 0 1em 0 !important;padding:0 5px;width:100%;');
      }

  }

  getSuppressLogoTemplate(): boolean {
    if (this.headerTemplate['suppressLogoTemplate']  && this.headerTemplate['suppressLogoTemplate'] ==='Y') {
      return true;
    }
    return  false;
  }

}
