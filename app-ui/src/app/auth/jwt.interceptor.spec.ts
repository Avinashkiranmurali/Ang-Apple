import { JwtInterceptor } from './jwt.interceptor';
import { AuthenticationService } from './authentication.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { Router } from '@angular/router';

describe('JwtInterceptor', () => {
  let jwtInterceptor: JwtInterceptor;
  let authService: AuthenticationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule
      ],
      providers: [
        AuthenticationService,
        { provide: JwtInterceptor },
        { provide: Router, useValue: {
          navigate: jasmine.createSpy('navigate') }
        },
        {
          provide: HTTP_INTERCEPTORS,
          useClass: JwtInterceptor,
          multi: true
        }
      ]
    });
    jwtInterceptor = TestBed.inject(JwtInterceptor);
    authService = TestBed.inject(AuthenticationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    sessionStorage.removeItem('curentToken');
    httpMock.verify();
  });

  it('should create an instance', () => {
    expect(jwtInterceptor).toBeTruthy();
  });

  it('should make http call to get XSRFToken with tokenData', () => {
    const currentToken = '9f6afef8-b4ff-40af-adab-db9f6bc71165';
    spyOnProperty(authService, 'currentTokenValue').and.returnValue(currentToken);
    authService.getXSRF().subscribe( res => {
      expect(res).toBeTruthy();
    });
    const httpReq = httpMock.expectOne('/apple-gr/service/getXSRFToken');
    expect(httpReq.request.headers.has('Content-type')).toEqual(true);
  });

  it('should make http call to get XSRFToken without tokenData', () => {
    const currentToken = null;
    spyOnProperty(authService, 'currentTokenValue').and.returnValue(currentToken);
    authService.getXSRF().subscribe( res => {
      expect(res).toBeTruthy();
    });
    const httpReq = httpMock.expectOne('/apple-gr/service/getXSRFToken');
    expect(httpReq.request.headers.has('Content-type')).toEqual(true);
  });

  it('should check for url header for payment server', () => {
    const url = '/apple-gr/service/paymentserver/api/payment/';
    expect(jwtInterceptor.isHeaderNeeded(url)).toEqual(false);
  });
});
