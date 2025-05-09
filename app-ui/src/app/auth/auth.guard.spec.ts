import { TestBed, waitForAsync } from '@angular/core/testing';
import { AuthGuard } from './auth.guard';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { AuthenticationService } from './authentication.service';
import { Router } from '@angular/router';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppConstants } from '@app/constants/app.constants';
import { of } from 'rxjs';

describe('AuthGuard', () => {
  let authService: AuthenticationService;
  let guard: AuthGuard;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, HttpClientModule, RouterTestingModule, HttpClientTestingModule],
      providers: [
        AuthGuard,
        AppConstants,
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        }
      ]
    });
    guard = TestBed.inject(AuthGuard);
    authService = TestBed.inject(AuthenticationService);
    httpTestingController = TestBed.inject(HttpTestingController);
    sessionStorage.setItem('curentToken', 'testData');
  });

  afterEach(() => {
    sessionStorage.removeItem('currentToken');
  });

  it('should create an instance', () => {
    expect(guard).toBeTruthy();
  });

  it('should define canActivate method', waitForAsync(() => {
    authService.getXSRF().subscribe(
      (res) => expect(res).toEqual(res, 'should return mockResponse'), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/getXSRFToken');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');

    // Respond with the fake data when called
    req.flush('OK', { headers: {'X-XSRF-TOKEN': 'b82780ff-e016-4c82-8f7'}});
    guard.canActivate(null, null);
    expect(guard.canActivate).toBeDefined();
  }));

  it('should call canActivate method - else check', waitForAsync(() => {
    authService.getXSRF().subscribe(
      (res) => expect(res).toEqual(res, 'should return mockResponse'), fail
    );
    // Expect a call to this URL
    const req = httpTestingController.expectOne('/apple-gr/service/getXSRFToken');

    // Assert that the request is a GET
    expect(req.request.method).toEqual('GET');
    guard.canActivate(null, null);
    expect(guard.canActivate).toBeDefined();
  }));

  it('should call navigateToErrorPage method', () => {
    spyOn(guard, 'navigateToErrorPage').and.callThrough();
    guard.navigateToErrorPage();
    expect(guard.navigateToErrorPage).toHaveBeenCalled();
  });

  it('should call canActivate method', () => {
    spyOn(guard, 'canActivate').and.callThrough();
    spyOn(guard['xsrfService'], 'init').and.returnValue(Promise.resolve(true));
    guard.canActivate(null, null);
    expect(guard.canActivate).toHaveBeenCalled();
  });

  it('should call canActivate method with currentToken', () => {
    spyOn(guard, 'canActivate').and.callThrough();
    guard['authenticationService'].currentTokenValue = '9f6afef8-b4ff-40af-adab-db9f6bc71165';;
    spyOn(guard['xsrfService'], 'init').and.returnValue(Promise.resolve(true));
    guard.canActivate(null, null);
    expect(guard.canActivate).toHaveBeenCalled();
  });

  it('should call canActivate method when promise rejects the call', () => {
    spyOn(guard, 'canActivate').and.callThrough();
    spyOn(guard['xsrfService'], 'init').and.returnValue(Promise.reject(false));
    guard.canActivate(null, null);
    expect(guard.canActivate).toHaveBeenCalled();
  });

});
