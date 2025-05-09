import { TestBed } from '@angular/core/testing';
import { PaymentStoreService } from './payment-store.service';

describe('PaymentStoreService', () => {
  let paymentStoreService: PaymentStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    paymentStoreService = TestBed.inject(PaymentStoreService);
  });

  it('should be created', () => {
    expect(paymentStoreService).toBeTruthy();
  });

  it('should call getIniital Method', () => {
    spyOn(paymentStoreService, 'getInitial').and.callThrough();
    paymentStoreService.getInitial();
    expect(paymentStoreService.getInitial).toHaveBeenCalled();
  });

  it('should call getObservable Method', () => {
    spyOn(paymentStoreService, 'getObservable').and.callThrough();
    paymentStoreService.getObservable();
    expect(paymentStoreService.getObservable).toHaveBeenCalled();
  });

  it('should call set state Method', () => {
    spyOn(paymentStoreService, 'set').and.callThrough();
    paymentStoreService.set(paymentStoreService.getInitial());
    expect(paymentStoreService.set).toHaveBeenCalled();
  });

  it('should call get state Method', () => {
    spyOn(paymentStoreService, 'get').and.callThrough();
    paymentStoreService.get();
    expect(paymentStoreService.get).toHaveBeenCalled();
  });
});
