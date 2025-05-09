import { CurrencyFormatPipe } from './currency-format.pipe';
import { User } from '@app/models/user';
import { UserStoreService } from '@app/state/user-store.service';
import { TestBed } from '@angular/core/testing';

describe('CurrencyFormatPipe', () => {
  let pipe: CurrencyFormatPipe;
  const user: User = require('assets/mock/user.json');
  let userStore: UserStoreService;
  const programData = require('assets/mock/program.json');
  const userData = {
    user : require('assets/mock/user.json'),
    program: programData,
    config: programData['config']
  };
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: UserStoreService, useValue: userData }
      ]
    })
    .compileComponents();
    userStore = TestBed.inject(UserStoreService);
    pipe = new CurrencyFormatPipe(userStore);
  });
  it('create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should format currency if format is points_decimal', () => {
    expect(pipe.transform(5, 'points_decimal', user.locale)).toBe('$0.05');
  });

  it('should format currency if format is points_decimal', () => {
    expect(pipe.transform(5, '', user.locale)).toBe('5');
  });

  afterEach(() => {
    pipe = null;
  });
});
