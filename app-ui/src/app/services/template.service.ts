import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { TemplateStoreService } from '../state/template-store.service';
import { UserStoreService } from '../state/user-store.service';
import { merge } from 'lodash';
import { BaseService } from './base.service';
import { User } from '@app/models/user';
import { ConfigData } from '@app/models/config-data';
import { Config } from '@app/models/config';

@Injectable({
  providedIn: 'root'
})

export class TemplateService extends BaseService {

  user: User;
  config: Config;
  template: {};

  constructor(
    private http: HttpClient,
    private templateStoreService: TemplateStoreService,
    public userStore: UserStoreService
  ) {
    super();
  }

  /**
   * Get Template
   *
   * @returns {Observable<ConfigData>}
   */
  getTemplate(): Observable<ConfigData> {
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    const url = this.baseUrl + 'configData';
    return this.http.get<ConfigData>(url, this.httpOptions)
      .pipe(
        map((response: ConfigData) => {

          const data = response;
          const varConfigData = data.configData;
          let mergedData = varConfigData;

          if (this.config['loginRequired']) {
            const anonPrgConfigData = (data.anon) ? data.anon.configData : {};
            mergedData = merge(varConfigData, anonPrgConfigData);
          }

          // merge all selected program data with var data
          this.template = mergedData;
          this.config['buttonColor'] = this.getTemplatesProperty('buttonColor');

          // load extra files for each var | program
          /* const extraFiles = (mergedData['additional']) ? ((mergedData['additional']['files']) ? mergedData['additional']['files'] : null) : null;
          if (extraFiles) {
          } */

          this.templateStoreService.addTemplate(this.template);
          return response;
        }),
        catchError((error: HttpErrorResponse) => {
          // general and service level error actions
          this.handleError(error);
          return throwError(error);
        })
      );
  }

  getProperty(prop) {
    return (this.template[prop]) ? this.template[prop] : null;
  }

  getTemplatesProperty(prop) {
    const tObj = (this.template && Object.keys(this.template).length > 0 && this.template.hasOwnProperty('templates')) ? this.template['templates'] : null;
    let data = null;
    if (tObj) {
      data = (tObj[prop]) ? this.template['templates'][prop] : null;
    }
    return data;
  }

  getBtnColor() {
    const btnColor = this.getTemplatesProperty('buttonColor');
    return btnColor !== null ? btnColor : '';
  }

}