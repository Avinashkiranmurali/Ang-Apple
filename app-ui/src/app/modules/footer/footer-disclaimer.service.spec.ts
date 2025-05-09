import { TestBed } from '@angular/core/testing';
import { FooterDisclaimerService } from './footer-disclaimer.service';
import { RouterTestingModule } from '@angular/router/testing';
import { UserStoreService } from '@app/state/user-store.service';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { of, ReplaySubject } from 'rxjs';

describe('FooterDisclaimerService', () => {
  let service: FooterDisclaimerService;
  const programData = require('assets/mock/program.json');
  programData['config']['displayFooterWatchExtendedDisclaimer'] = true;
  const userData = {
    user: require('assets/mock/user.json'),
    program: programData,
    config: programData.config
  };
  const eventSubject = new ReplaySubject<NavigationEnd>(1);
  const mockRouter = {
    events: eventSubject.asObservable(),
    url: '/watch',
    navigate: jasmine.createSpy('navigate')
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [
        { provide: UserStoreService, useValue : userData },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: {
          params: of({category: 'watch', subcat: ''}) }
        }
      ]
    });
    service = TestBed.inject(FooterDisclaimerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call isWatchLanding', () => {
    spyOn(service, 'isWatchLanding').and.callThrough();
    service.isWatchLanding();
    expect(service.isWatchLanding).toHaveBeenCalled();
  });

});
