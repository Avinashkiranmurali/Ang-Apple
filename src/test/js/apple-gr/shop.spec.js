'use strict';

beforeEach(angular.mock.module('appleCore'));
beforeEach(angular.mock.module('merch.services'));
beforeEach(angular.mock.module('merch.controllers'));
beforeEach(angular.mock.module('decor.products'));

beforeEach(angular.mock.module('mocks'));


describe('decorate product objects', function () {
    var param = '/?categorySlug=ipod-touch';
    var scope;
    var configService;
    var createService;
    var mockService;
    var decorService
    var createController;
    var $httpBackend;
    var factory;
    var runConfig;
    var $log, $filter;

    beforeEach(function () {

        module('appleCore');

        inject(function ( _$log_, _$filterFilter_) {
            $filter = _$filterFilter_;
            $log = _$log_;
        });
    });

    beforeEach(inject(function () {
        var $injector = angular.injector(['mock.products']);
        createService = function () {
            return $injector.get('mProducts');
        };
        mockService = createService();
    }));
    beforeEach(inject(function () {
        var $injector = angular.injector(['decor.products']);
        createService = function () {
            return $injector.get('productDecor');
        };
        decorService = createService();
    }));

    beforeEach(inject(function ($injector) {
        //setup mock http responses
        $httpBackend = $injector.get('$httpBackend');
        //backend definition
        var serviceCall = $httpBackend.when('GET', '/apple-gr/service/productsWithConfiguration/?categorySlug=ipod-touch')
            .respond(mockService.configuration);
    }));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();
        createController = $controller('configTemplateCtrl', {
            $scope: scope
        });
        runConfig = scope.productConfigurable('ipod','ipod-touch');
    }));

    afterEach(function () {
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    describe('configTemplateCtrl',function(){
        it('should initialize with no errors', function () {
            expect(runConfig).toEqual(true);
            expect(scope.disabled).toEqual(true);
            $httpBackend.flush();

        });
        it('should get products with options', function () {
            $httpBackend.expectGET('/apple-gr/service/productsWithConfiguration/?categorySlug=ipod-touch');
            //var controller = createController;
            expect(scope.disabled).toBeDefined;
            $httpBackend.flush();
            console.log(scope.options)
        });


    })

});
