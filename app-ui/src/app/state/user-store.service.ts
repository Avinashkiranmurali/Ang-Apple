import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { environment } from '../../environments/environment';
import { merge, assign } from 'lodash';
import { Config } from '@app/models/config';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';

@Injectable({
  providedIn: 'root'
})

export class UserStoreService {

  // - We set the initial state in BehaviorSubject's constructor
  // - Nobody outside the Store should have access to the BehaviorSubject
  //   because it has the write rights
  // - Writing to state should be handled by specialized Store methods (ex: addNav, removeNav, etc)
  // - Create one BehaviorSubject per store entity, for example if you have NavGroups
  //   create a new BehaviorSubject for it, as well as the observable$, and getters/setters
  // eslint-disable-next-line @typescript-eslint/naming-convention,no-underscore-dangle,id-blacklist,id-match
  private readonly _user = new BehaviorSubject<User>({} as User);
  // eslint-disable-next-line @typescript-eslint/naming-convention,no-underscore-dangle,id-blacklist,id-match
  private readonly _program = new BehaviorSubject<Program>({} as Program);
  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  private readonly _config = new BehaviorSubject<Config>( {} as Config);

  // Expose the observable$ part of the _user subject (read only stream)
  readonly user$ = this._user.asObservable();
  readonly program$ = this._program.asObservable();
  readonly config$ = this._user.asObservable();

  isEnableNotificationRibbon: boolean;
  isShowNotificationRibbon: boolean;
  notificationRibbonMessage: string;
  detailsname: string;

  get user(): User {
    return this._user.getValue();
  }

  set user(val: User) {
    this._user.next(val);
  }

  get(){
    return this.user$;
  }
  get program(): Program {
    return this._program.getValue();
  }

  set program(val: Program) {
    this._program.next(val);
    this.addConfig(this._program.getValue()['config']);
  }

  get config(): Config {
    return  this._config.getValue();
  }

  set config(val: Config) {
    this._config.next(val);
  }

  addUser(data: User) {
    this.user = data;
    if (Object.keys(this.program).length > 0) {
      this.user = merge(this.user, { program: this.program});
    }
  }

  addProgram(data: Program) {
    this.program = data;
    if (Object.keys(this.user).length > 0) {
      this.user = merge(this.user, { program: this.program});
    }
  }

  addConfig(config: Config) {
    this.config = config;
    if (!environment.production && this.config) {
      this.config['imageServerUrl'] = '/imageserver';
    }
    this.config['welcomeMessageBackgroundColorOnMobile'] = this.config['welcomeMessageBackgroundColorOnMobile'] ? this.config['welcomeMessageBackgroundColorOnMobile'] : this.config['clientHeaderBackgroundColor'] ;
    if (this.user['anonymous']) {
      this.config['loginRequired'] = (this.config['login_required'] !== undefined) ? this.config['login_required'] : true;
    } else {
      this.config['loginRequired'] = (this.config['login_required'] !== undefined) ? this.config['login_required'] : false;
    }
    this.config['navBarColors'] = {
      textColor: this.config['navBarTextColor'],
      hoverColor: this.config['navBarTextHoverColor'],
      activeTextColor: this.config['navBarTextHoverColor']
    };
    this.config['subNavColors'] = {
      textColor: this.config['subNavTextColor'],
      hoverColor: this.config['subNavHoverTextColor'],
      activeTextColor: this.config['subNavHoverTextColor']
    };
    this.config['ribbonCloseIconColor'] = this.config['ribbonCloseIconColor'] ? this.config['ribbonCloseIconColor'] : this.config['ribbonTextColor'] ;
    this.config['isAgent'] = Boolean(this.user.agentId);
    this.config['viewOnly'] = (this.config['isAgent'] && this.config['obo_redeemable'] !== undefined) ? this.config['obo_redeemable'] === 'view_only' : false;
    this.config['sessionTimeout'] = this.config['sessionTimeout'] ? this.config['sessionTimeout'] : '';
    this.config['sessionTimeoutRemaining'] = (this.config['sessionTimeout'] && this.config['sessionTimeoutWarning']) ? this.config['sessionTimeoutWarning'] : '';
    this.config['sessionTimeoutWarning'] = (this.config['sessionTimeout'] && this.config['sessionTimeoutWarning']) ? (this.config['sessionTimeout'] - this.config['sessionTimeoutWarning']) : '';
  }

  // To Trigger Notification Ribbon Banner
  enableNotificationBanner(bool, msg) {
    if (bool === true) {
       this.isEnableNotificationRibbon = true;
    }
    this.isShowNotificationRibbon = bool;
    this.notificationRibbonMessage = msg;
  }

  onNotificationRibbonClose(){
    this.isShowNotificationRibbon = false;
    setTimeout(() => {
      this.notificationRibbonMessage = '' ;
    }, 500);
  }

  isPageAccessible(pageName) {
    if (this.config.unAuthorizedPages) {
      if (this.config.unAuthorizedPages && this.config.unAuthorizedPages.indexOf(pageName) !== -1) {
        return false;
      }
    }
    return true;
  }

}
