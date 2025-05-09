import { TestBed, waitForAsync } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { GiftPromoService } from './gift-promo.service';
import { RouterTestingModule } from '@angular/router/testing';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { CartService } from '@app/services/cart.service';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TemplateStoreService } from '@app/state/template-store.service';
import { ModalsService } from '@app/components/modals/modals.service';
import { TranslateModule } from '@ngx-translate/core';
import { UserStoreService } from '@app/state/user-store.service';
import { TransitionService } from '@app/transition/transition.service';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';

describe('GiftPromoService', () => {
    let giftPromoService: GiftPromoService;
    let httpTestingController: HttpTestingController;
    // Fake response data
    const fakeResponse = require('assets/mock/cart.json')['cartItems'];
    const program = require('assets/mock/program.json');
    let cartService: CartService;
    const productDetails = require('assets/mock/product-detail.json');
    const mockAddCartResponse = require('assets/mock/addToCart.json');
    const programData = require('assets/mock/program.json');
    const mockUser = require('assets/mock/user.json');
    mockUser['program'] = programData;
    const userStoreData = {
        user: mockUser,
        program: programData,
        config: programData['config'],
        get: () => of(mockUser)
    };
    userStoreData.user['program'] = program;
    userStoreData.user['browseOnly'] = false;
    userStoreData.config['loginRequired'] = false;
    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                RouterTestingModule
            ],
            providers: [
                { provide: TransitionService },
                { provide: GiftPromoService },
                { provide: ParsePsidPipe },
                { provide: CurrencyPipe, useValue: {
                    program: of(program),
                    transform: () => of({}) }
                },
                { provide: NgbActiveModal },
                { provide: Router, useValue: {
                    url: 'store/cart',
                    navigate: jasmine.createSpy('navigate') }
                },
                { provide: ActivatedRoute, useValue: {
                    params: of({ category: 'ipad', subcat: 'ipad-air' }) }
                },
                { provide: TemplateStoreService, useValue: {
                    buttonColor: () => of({}) }
                },
                { provide: ModalsService, useValue: {
                    openAnonModalComponent: () => of({}),
                    openSuggestAddressModalComponent: () => of({}),
                    openEngraveModalComponent: () => {},
                    openBrowseOnlyComponent: () => ({}) }
                },
                { provide: UserStoreService, useValue: userStoreData },
                { provide: MatomoService, useValue: {
                    broadcast: () => {}
                }},
                { provide: CurrencyFormatPipe },
                { provide: DecimalPipe }
            ]
        });

        // Inject the http test controller
        httpTestingController = TestBed.inject(HttpTestingController);
        giftPromoService = TestBed.inject(GiftPromoService);
        cartService = TestBed.inject(CartService);
        giftPromoService.user = userStoreData.user;
        giftPromoService.config = program['config'];
        giftPromoService.config['loginRequired'] = false;
        giftPromoService.user['browseOnly'] = false;
    });

    afterEach(() => {
        // After every test, assert that there are no more pending requests.
        httpTestingController.verify();
    });

    it('should be created', () => {
        expect(giftPromoService).toBeTruthy();
    });

    it('get gift promo products response', waitForAsync(() => {
        const psid = '30001MXG22LL/A';
        const mockGiftProductResponse = require('assets/mock/gift-products.json');
        // Setup a request using the giftProducts data
        giftPromoService.getGiftPromoProducts(psid).subscribe(
            (products) => expect(products).toEqual(mockGiftProductResponse, 'should return mockGiftProductResponse'), fail
        );

        // Expect a call to this URL
        const req = httpTestingController.expectOne('/apple-gr/service/' + 'giftItem?qualifyingPsid=' + psid.replace(/-/g, '/'));

        // Assert that the request is a GET
        expect(req.request.method).toEqual('GET');

        // Respond with the fake data when called
        req.flush(mockGiftProductResponse);

        // Run some expectations
        expect(mockGiftProductResponse.length).toBe(3);
    }));

    it('should test for 404 error - getGiftPromoProducts', waitForAsync(() => {
        const psid = '30001MXG22LL/A';
        const errorMsg = 'deliberate 404 error';
        giftPromoService.getGiftPromoProducts(psid).subscribe(
          data => fail('should have failed with the 404 error'),
          (error: HttpErrorResponse) => {
            expect(error.status).toEqual(404, 'status');
            expect(error.error).toEqual(errorMsg, 'message');
          });
        // Expect a call to this URL
        const req = httpTestingController.expectOne('/apple-gr/service/' + 'giftItem?qualifyingPsid=' + psid.replace(/-/g, '/'));
        // Respond with mock error
        req.flush(errorMsg, { status: 404, statusText: 'Not Found' });

        // Assert that the request is a GET
        expect(req.request.method).toEqual('GET');
    }));
});
