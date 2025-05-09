import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import merge from 'lodash/merge';

@Injectable({
  providedIn: 'root'
})

export class TemplateStoreService {

  constructor() {}

  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  private readonly _template = new BehaviorSubject<object>({});

  readonly template$ = this._template.asObservable();

  get template(): object {
    return this._template.getValue();
  }

  set template(val: object) {
    this._template.next(val);
  }

  addTemplate(template: object) {
    this.template = merge(this.template, template);
    this.template = {...this.template};
  }
  get detailsTemplate(): object {
    return this._template.getValue()['templates']['detail'];
  }
  get cartTemplate(): object {
    return this._template.getValue()['templates']['cart'];
  }
  get footerTemplate(): object {
    return this._template.getValue()['templates']['footer'] || {};
  }
  get checkoutTemplate(): object {
    return this._template.getValue()['templates']['checkout'];
  }
  get orderDetailTemplate(): object {
    return  this._template.getValue()['templates']['orderStatus'];
  }

  get orderHistoryTemplate(): object {
    return this._template.getValue()['templates']['orderHistory'];
  }

  get orderConfirmationTemplate(): object {
    return this._template.getValue()['templates']['orderConfirmation'];
  }
  get userDropdownLinks(): object {
    // applicable values are ['bag', 'orders', 'navigate-back', 'home', 'sign-out', 'sign-in', 'terms', 'live-chat'];
    return this._template.getValue()['templates']['userDropdownLinks'];
  }

  get headerTemplate(): object | {}{
    return this._template.getValue()['templates']['header'] || {};
  }

  get anonymousModal(): object {
    const tempObj = this._template.getValue()['templates'];
    return (tempObj && Object.keys(tempObj).length > 0 && tempObj.hasOwnProperty('anonymousModal')) ? this._template.getValue()['templates']['anonymousModal'] : {};
   // return this._template.getValue()['templates']['anonymousModal'] || {};
  }

  get navigationTemplate(): object {
    return this._template.getValue()['templates']['navigation'];
  }

  get bodyTemplate(): object {
    return this._template.getValue()['templates']['body'];
  }

  get buttonColor(): string | null {
    return this._template.getValue()['templates']['buttonColor'] || null;
  }
}
