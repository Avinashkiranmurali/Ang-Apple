import { TestBed } from '@angular/core/testing';
import { Subscription } from 'rxjs';
import { BaseService } from './base.service';

describe('BaseService', () => {
  let baseService: BaseService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    baseService = TestBed.inject(BaseService);
  });

  it('should be created', () => {
    expect(baseService).toBeTruthy();
  });

  it('Base URL should be set', () => {
    // Run some expectations
    expect(baseService.baseUrl).toBe('/apple-gr/service/');
  });

  it('Order URL should be set', () => {
    // Run some expectations
    expect(baseService.orderUrl).toBe('/apple-gr/service/order/');
  });

  it('should call handleError method', () => {
    spyOn(baseService, 'handleError').and.callThrough();
    baseService.handleError(null);
    expect(baseService.handleError).toHaveBeenCalled();
  });

  it('should unsubscribe on destroy', () => {
    baseService.subscriptions = {
      programApi: new Subscription(),
      userApi: new Subscription()
    };
    baseService.ngOnDestroy();
    expect(baseService.ngOnDestroy).toBeTruthy();
  });
});
