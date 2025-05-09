import { CurrencyPipe } from './currency.pipe';
import { UserStoreService } from '@app/state/user-store.service';
import { TestBed, waitForAsync } from '@angular/core/testing';

describe('CurrencyPipe', () => {

  let userStore: UserStoreService;
  let currencyPipe: CurrencyPipe;
  const programData = require('assets/mock/program.json');
  const userData = {
    user : require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: UserStoreService, useValue: userData }
      ]
    })
    .compileComponents();
    userStore = TestBed.inject(UserStoreService);
    currencyPipe = new CurrencyPipe(userStore);
    userStore.config = userData.config;
    userStore.config.currencySymbol = undefined;
  }));

  afterEach(() => {
    userStore.config = userData.config;
    currencyPipe = null;
  });

  it('create an instance', () => {
    expect(currencyPipe).toBeTruthy();
  });

  it('format currency with wide symbol - useNarrowCurrencySymbol = true and digitsInfo for zh_TW', () => {
    userStore.config.useNarrowCurrencySymbol = true;
    expect(currencyPipe.transform(123, '', 'symbol', '1.0-0')).toBe('$123');
  });

  it('format currency with narrow symbol - useNarrowCurrencySymbol = false and digitsInfo for zh_TW', () => {
    userStore.config.useNarrowCurrencySymbol = false;
    expect(currencyPipe.transform(123, '', 'symbol', '1.0-0')).toBe('$123');
  });

  it('format currency for singapore locale - useNarrowCurrencySymbol = true', () => {
    userStore.config.useNarrowCurrencySymbol = true;
    userStore.config.currencySymbol = 'S$';
    expect(currencyPipe.transform(123)).toBe('S$123.00');
  });

  it('format currency for singapore locale - useNarrowCurrencySymbol = false', () => {
    userStore.config.useNarrowCurrencySymbol = false;
    userStore.config.currencySymbol = 'S$';
    expect(currencyPipe.transform(123)).toBe('S$123.00');
  });
});
