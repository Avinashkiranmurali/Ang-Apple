'use strict';

beforeEach(angular.mock.module('appleCore'));
beforeEach(angular.mock.module('merch.services'));
beforeEach(angular.mock.module('merch.controllers'));
beforeEach(angular.mock.module('merch.directives'));

beforeEach(angular.mock.module('mocks'));


beforeEach(function(){
   var config = {
       catalogName: 'Apple Store',
       topCategory: 'Electronics',
       priceConfig: 'pointsOnly',
       errorMsg: '',
       category: '',
       showPoints: '',
       pointsBalance: undefined,
       pointLabel: '',
       categoryName: '',
       slug: '',
       subcat: '',
       subcatName: '',
       subnav: [],
       products: [],
       results: [],
       totalItems: '',
       items: [],
       navitem: [],
       details: [],
       psid: ''
   }
});

describe('siteCtrl', function () {

    var $httpBackend;
    var scope;
    var user;
    var cart;
    var createService;
    var factory;
    var createController;
    var $resource;

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();
        createController = $controller('siteCtrl', {
            $scope: scope
        });
    }));

    afterEach(function () {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    describe('getting user objects', function () {

        it('should get and set page variables for user and program', function () {
            $httpBackend.expectGET('/apple-gr/customer/user.json');
            //var controller = createController;
            $httpBackend.flush();

            expect(scope.firstname).toBeDefined();
        });
        it('should fail on bad URL', function () {
            $httpBackend.expectGET('/apple-gr/customer/user.json').respond(400, '');
            //var controller = createController;
            $httpBackend.flush();

            expect(scope.errorMsg).toContain('Error');
        });
    });

    describe('adding items to cart', function() {
        var goodPsid = '30700MD101LL/A',
            badPsid = 'MD101LL/A';

        it('should add item to cart with item psid', function () {
            $httpBackend.expect('GET', '/apple-gr/service/cart/add/'+ goodPsid).respond(200,'');
            scope.addToCart(goodPsid);
            $httpBackend.flush();

            expect(scope.cartResponse).toBeDefined('200');
        });

        // Waiting for Back-end to send a status response other than 200 for bad psid
        it('should fail on bad psid', function () {
            $httpBackend.expect('GET','/apple-gr/service/cart/add/'+ badPsid).respond(400,'');
            scope.addToCart(badPsid);
            $httpBackend.flush();

            expect(scope.errorMsg).toContain('Error');
        });
    });
});

    describe('load more products factory', function () {

        var results = [{psid:'1/a'},{psid:'2/a'},{psid:'3/a'},{psid:'4/a'},{psid:'5/a'},{psid:'6/a'},{psid:'7/a'},{psid:'8/a'},{psid:'9/a'},{psid:'10/a'},{psid:'11/a'},{psid:'12/a'},{psid:'13/a'},{psid:'14/a'},{psid:'15/a'},{psid:'16/a'},{psid:'17/a'},{psid:'18/a'},{psid:'19/a'},{psid:'20/a'}];
        var items = [{psid:'1/b'},{psid:'2/b'},{psid:'3/b'},{psid:'4/b'},{psid:'5/b'},{psid:'6/b'},{psid:'7/b'},{psid:'8/b'},{psid:'9/b'}]
        var empty = [];
        var loader;
        module('merch.services');
        /*beforeEach(function(){
            inject(function($injector){
                loader = $injector.get('productLoader');
            })
        });
        it('load the initial 15 products to a results page', function () {
            //check the loader only brings back 15 results starting at 0 (empty array []).
            expect(loader(20,results,empty).length).toEqual(15);
        });

        it('load the rest of the products to the same results', function () {
            //append 9 more products to an existing set.
            //check the loader only brings back 15 results starting at 0 (empty array []).
            expect(loader(20,results,items).length).toEqual(20);
        });*/

    });

describe('Modal Controller', function () {

    var $httpBackend;
    var scope;
    var user;
    var createService;
    var createController;
    var modalInstance;
    var templateBody;
    var pointsUsed = 500;
    var pointsPurchase = 1000;
    var maxPurchaseTotal = 899;

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();
        modalInstance = {                    // Create a mock object using spies
            close: jasmine.createSpy('modalInstance.close'),
            dismiss: jasmine.createSpy('modalInstance.dismiss'),
            result: {
                then: jasmine.createSpy('modalInstance.result.then')
            }};
        createController = $controller('ModalCtrl', {
            $scope: scope, $modalInstance: modalInstance, templateBody: function () {
                return attrs.modalBody;
            },
            totals: function () {
                scope.modalData = {
                    useRewards: pointsUsed,
                    purchaseRewards: pointsPurchase,
                    purchaseTotal: maxPurchaseTotal
                };
                return scope.modalData;
            }
        });
    }));


    describe('Initial state, open, close', function () {
        it('should instantiate the controller properly', function () {
            expect(createController).not.toBeUndefined();
        });

        it('should close the modal with result "cancel" when dismissed', function () {
            scope.cancel();
            expect(modalInstance.dismiss).toHaveBeenCalledWith('cancel');
        });
        it('should close the modal with result "close" when closed', function () {
            scope.closeModal();
            expect(modalInstance.close).toHaveBeenCalledWith('close');
        });


    });
});

describe('Navigation and Routes', function () {


    var scope, ctrl, $rootScope, $state, $injector, config,
        state = 'store.browse', substate = 'store.browse.results',
        cart = 'store.cart', details = 'store.browse.results.detail';

    beforeEach(function () {

        module('appleCore');

        inject(function (_$rootScope_, _$state_, _$injector_, $templateCache) {
            $rootScope = _$rootScope_;
            $state = _$state_;
            $injector = _$injector_;

        });
    });

    it('should respond to URL', function () {

        expect($state.href('store')).toEqual('/store/');
        expect($state.href(state, { category: 'mac' })).toEqual('/store/browse/mac/');
        expect($state.href(substate, {category: 'mac', subcat: 'imac' })).toEqual('/store/browse/mac/imac/');
        expect($state.href(details, {category: 'mac', subcat: 'macbook_air',psid: 'MD711LL-B' })).toEqual('/store/browse/mac/macbook_air/MD711LL-B/');
        expect($state.href(cart)).toEqual('/store/cart/');

    });
  /* trying to test the resolve functions in the app.js */
    it('should resolve functions before ', inject(function ($controller) {
        $state.go(substate,{category: 'mac', subcat: 'imac' });
        $rootScope.$apply();

        ctrl = $controller('siteCtrl', {
            '$scope': $rootScope, 'config': {catalogName: 'Apple Store'}
        });
        $rootScope.$apply();

       // expect($rootScope.subnav).toBeDefined();

    }));

});
describe('CartCtrl', function () {

    var $httpBackend;
    var scope;
    var user;
    var cart;
    var createService;
    var factory;
    var createController;
    var $resource;

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();
        createController = $controller('CartCtrl', {
            $scope: scope
        });
    }));

    describe('getting cart objects', function () {

        /*it('should get and set page variables', function () {

        });*/

    });

});

describe('Search Controller', function () {
    var params;
    var scope;
    var configService;
    var createController;

    module('merch.services');
    beforeEach(function(){
        inject(function($injector){
            configService = $injector.get('configService');
        })
    });
    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();
        createController = $controller('SearchCtrl', {
            $scope: scope
        });
        scope.terms = 'mac'
    }));
    describe('scope initialization',function(){
        it('should show loading image while loading', function () {
            expect(scope.dataLoading).toEqual(true);
        });
        it('should get product data', function () {
            expect(scope.search('mac')).toBeDefined();

        });
    })

});



