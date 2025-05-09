import { Injectable } from '@angular/core';
import { UserStoreService } from '@app/state/user-store.service';
import { Config } from '@app/models/config';
import { Program } from '@app/models/program';

export interface Five9SocialWidget {
  addWidget: (options: object) => void;
  maximizeChat: (options: object) => void;
  options: {};
  widgetAdded: boolean;
  frame: HTMLElement;
}

@Injectable({
  providedIn: 'root'
})
export class Five9Service {

  config: Config;
  program: Program;
  constructor(
    private userStore: UserStoreService
  ) {
    this.config = this.userStore.config;
    this.program = this.userStore.program;
    if (this.program.sessionConfig.five9Config.chatEnabled) {
      this.loadFive9Script().onload = () => {
        window.Five9SocialWidget.addWidget({
          type: this.program.sessionConfig.five9Config.type,
          rootUrl: this.program.sessionConfig.five9Config.rootUrl,
          tenant: this.program.sessionConfig.five9Config.tenant,
          title: this.program.sessionConfig.five9Config.title,
          showProfiles: false,
          profiles: [this.program.sessionConfig.five9Config.profile],
          theme: location.href.split('/').slice(0, 5).join('/') + '/assets/five9/default-theme.css'
        });
      };
    }
  }

  loadFive9Script() {
    const body = document.getElementsByTagName('body')[0];
    const scriptElememt = document.createElement('script');
    scriptElememt.src = 'assets/five9/five9-social-widget.js' + '?' + this.program.sessionConfig.buildId;
    scriptElememt.type = 'text/javascript';
    body.appendChild(scriptElememt);
    return scriptElememt;
  }
}
