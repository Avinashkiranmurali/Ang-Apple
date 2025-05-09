import { NumberPipe } from '@app/pipes/number.pipe';
import { UserStoreService } from '@app/state/user-store.service';
import { TestBed, waitForAsync } from '@angular/core/testing';

describe('NumberPipe', () => {
  let userStore: UserStoreService;
  let numberPipe: NumberPipe;
  const userData = {
    user : require('assets/mock/user.json'),
  };
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: UserStoreService, useValue: userData },
      ]
    })
    .compileComponents();
    userStore = TestBed.inject(UserStoreService);
    numberPipe = new NumberPipe(userStore);
  }));

  it('create an instance', () => {
    expect(numberPipe).toBeTruthy();
  });

  it('format the number with NumberPipe', () => {
    expect(numberPipe.transform(123)).toBe('123');
  });
});
