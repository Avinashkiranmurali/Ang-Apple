import { Injectable, EventEmitter, Output } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { STATE } from '@app/constants/app.constants';

@Injectable({
  providedIn: 'root'
})
export class PaymentStoreService {

  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  public state$: Observable<any>;
  private readonly stateEvent = new BehaviorSubject<any>({});
  @Output() public OnSettingsChange = new EventEmitter<any>();

  constructor() {
    this.state$ = this.stateEvent.asObservable();
  }

  getObservable() {
    return this.state$;
  }

  set(newState) {
    this.stateEvent.next(newState);
  }

  get() {
    return this.state$;
  }

  getInitial() {
    const stateValue = STATE;
    stateValue['selections'] = {};
    stateValue['cart'] = {};

    const initState = new BehaviorSubject<any>(stateValue);
    this.stateEvent.next(STATE);
    return initState.value;
  }
}
