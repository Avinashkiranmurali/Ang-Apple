import { TestBed, waitForAsync } from '@angular/core/testing';

import { GridEventHandlerService } from './grid-event-handler.service';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';

describe('GridEventHandlerService', () => {
  let service: GridEventHandlerService;
  let notificationRibbonService: NotificationRibbonService;
  const facetsScrollObj = {
    notificationRibbonEnabled: false,
    fixedFacetsContainer: false,
    endPositionFacetsContainer: false,
    scrollTopPrev: 0,
    scrollTop: 0,
    transition: 0,
    maxTransition: 0,
    scrollStarts: 326,
    topFreezedElementHeight: 0,
    filterBarTop: 0
  };
  const clientReactMock = {
    top: -0.9895833730697632
  } as unknown as ClientRect;
  const querySelectorMock = {
    getBoundingClientRect: () => {
      {
        return clientReactMock;
      }
    }
  } as unknown as Element;
  const elementByClassNameMock = [{
    offsetHeight: 2100,
    setAttribute: jasmine.createSpy('setAttribute')
  }] as unknown as HTMLCollectionOf<Element>;
  const getMockHTMLElement = (prop: { [key: string]: number }) => prop as unknown as HTMLElement;
  const getMockHTMLCollectionOfElement = (prop: { [key: string]: number }[]) => prop as unknown as HTMLCollectionOf<Element>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        ParsePsidPipe,
        NotificationRibbonService,
      ]
    });
    service = TestBed.inject(GridEventHandlerService);
    notificationRibbonService = TestBed.inject(NotificationRibbonService);
    spyOn(service.document, 'querySelector').and.returnValue(querySelectorMock);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get offsetHeight value equal to 4086 when getFacetsContainerFullView is called', () => {
    const mockHTMLCollectionOfElementValue = [{
      offsetHeight: 4086
    }];
    spyOn(service.document, 'getElementsByTagName').and.returnValue(getMockHTMLCollectionOfElement(mockHTMLCollectionOfElementValue));
    const value: any = service.getFacetsContainerFullView();
    expect(value.offsetHeight).toEqual(4086);
  });

  it('should get empty string when getFacetsContainerFullView is called', () => {
    spyOn(service.document, 'getElementsByTagName').and.returnValue([] as unknown as HTMLCollectionOf<Element>);
    const value = service.getFacetsContainerFullView();
    expect(value).toEqual('');
  });

  it('should get offsetHeight value equal to 4086 when getFacetsContainerView is called', () => {
    const mockHTMLCollectionOfElementValue = [{
      offsetHeight: 1056
    }];
    spyOn(service.document, 'getElementsByClassName').and.returnValue(getMockHTMLCollectionOfElement(mockHTMLCollectionOfElementValue));
    const value: any = service.getFacetsContainerView();
    expect(value.offsetHeight).toEqual(1056);
  });

  it('should get empty string when getFacetsContainerView is called', () => {
    spyOn(service.document, 'getElementsByClassName').and.returnValue([] as unknown as HTMLCollectionOf<Element>);
    const value = service.getFacetsContainerView();
    expect(value).toEqual('');
  });

  it('should set facetsScrollObj.endPositionFacetsContainer equal to false when facetsScroll  is called', () => {
    const data = { ...facetsScrollObj };
    const mockHTMLCollectionOfElementValue = [{
      offsetHeight: 1500
    }];
    const mockHTMLElement = { offsetHeight: 1000, scrollHeight: 1637 };
    spyOn(service.document, 'getElementsByClassName').and.returnValue(getMockHTMLCollectionOfElement(mockHTMLCollectionOfElementValue));
    spyOn(service, 'getGrid').and.returnValue(getMockHTMLElement(mockHTMLElement));
    spyOn(service, 'getFacetsContainerFullView').and.returnValue(getMockHTMLElement({offsetHeight: 1500}));
    const value = service.facetsScroll(true, data);
    expect(value.endPositionFacetsContainer).toBeFalsy();
  });

  it('should set facetsScrollObj.fixedFacetsContainer equal to true when facetsScroll  is called', () => {
    const data = { ...facetsScrollObj, transition: 200, filterBarTop: 30 };
    const mockGridHTMLElementValue = { offsetHeight: 1500, scrollHeight: 1637 };
    const mockFacetContainerHTMLElementValue = { offsetHeight: 1000 };
    spyOnProperty(document.documentElement, 'scrollTop', 'get').and.returnValue(401);
    (window['scrollY'] as any) = 401;
    (window['innerHeight'] as any) = 350;
    spyOn(service.document, 'getElementsByClassName').and.returnValue(elementByClassNameMock);
    spyOn(service, 'getGrid').and.returnValue(getMockHTMLElement(mockGridHTMLElementValue));
    spyOn(service, 'getFacetsContainerFullView').and.returnValue(getMockHTMLElement(mockFacetContainerHTMLElementValue));
    const value = service.facetsScroll(true, data);
    expect(value.fixedFacetsContainer).toBeTruthy();
  });

  it('should set facetsScrollObj.fixedFacetsContainer equal to true when facetsScroll  is called', () => {
    const data = { ...facetsScrollObj, scrollTop: 401, filterBarTop: 30 };
    const mockGridHTMLElementValue = { offsetHeight: 1500, scrollHeight: 1637 };
    const mockFacetContainerHTMLElementValue = { offsetHeight: 1000 };
    spyOnProperty(document.documentElement, 'scrollTop', 'get').and.returnValue(400);
    (window['scrollY'] as any) = 400;
    (window['innerHeight'] as any) = 1600;
    spyOn(service.document, 'getElementsByClassName').and.returnValue(elementByClassNameMock);
    spyOn(service, 'getGrid').and.returnValue(getMockHTMLElement(mockGridHTMLElementValue));
    spyOn(service, 'getFacetsContainerFullView').and.returnValue(getMockHTMLElement(mockFacetContainerHTMLElementValue));
    const value = service.facetsScroll(true, data);
    expect(value.fixedFacetsContainer).toBeTruthy();
  });

  it('should set facetsScrollObj.transition equal to 0 when facetsScroll  is called', () => {
    const data = { ...facetsScrollObj, scrollTop: 401, filterBarTop: 30, maxTransition: 500 };
    const mockGridHTMLElementValue = { offsetHeight: 1500, scrollHeight: 1637 };
    const mockFacetContainerHTMLElementValue = { offsetHeight: 1000 };
    spyOnProperty(document.documentElement, 'scrollTop', 'get').and.returnValue(401);
    (window['scrollY'] as any) = 401;
    (window['innerHeight'] as any) = 1600;
    spyOn(service.document, 'getElementsByClassName').and.returnValue(elementByClassNameMock);
    spyOn(service, 'getGrid').and.returnValue(getMockHTMLElement(mockGridHTMLElementValue));
    spyOn(service, 'getFacetsContainerView').and.returnValue(getMockHTMLElement(mockFacetContainerHTMLElementValue));
    spyOn(service, 'getFacetsContainerFullView').and.returnValue(getMockHTMLElement(mockFacetContainerHTMLElementValue));
    const value = service.facetsScroll(true, data);
    expect(value.transition).toEqual(0);
    expect(service.facetsScroll(false, data).fixedFacetsContainer).toBeTruthy();
  });

  it('should set facetsScrollObj.fixedFacetsContainer equal to false when facetsScroll  is called', () => {
    const data = { ...facetsScrollObj, filterBarTop: 30, maxTransition: 500 };
    const mockGridHTMLElementValue = { offsetHeight: 1500, scrollHeight: 1637 };
    const mockFacetContainerHTMLElementValue = { offsetHeight: 1000 };
    spyOnProperty(document.documentElement, 'scrollTop', 'get').and.returnValue(80);
    (window['scrollY'] as any) = 80;
    (window['innerHeight'] as any) = 4600;
    spyOn(service.document, 'getElementsByClassName').and.returnValue(elementByClassNameMock);
    spyOn(service, 'getGrid').and.returnValue(getMockHTMLElement(mockGridHTMLElementValue));
    spyOn(service, 'getFacetsContainerView').and.returnValue(getMockHTMLElement(mockFacetContainerHTMLElementValue));
    spyOn(service, 'getFacetsContainerFullView').and.returnValue(getMockHTMLElement(mockFacetContainerHTMLElementValue));
    const value = service.facetsScroll(true, data);
    expect(value.fixedFacetsContainer).toBeFalsy();
    expect(service.facetsScroll(false, data).fixedFacetsContainer).toBeFalsy();
  });

  it('should set facetsScrollObj.fixedFacetsContainer equal to false when facetsScroll  is called', () => {
    const data = { ...facetsScrollObj, filterBarTop: 30, maxTransition: 500 };
    const mockGridHTMLElementValue = { offsetHeight: 1500, scrollHeight: 1637 };
    const mockFacetContainerHTMLElementValue = { offsetHeight: 1500 };
    spyOnProperty(document.documentElement, 'scrollTop', 'get').and.returnValue(400);
    (window['scrollY'] as any) = 80;
    (window['innerHeight'] as any) = 1400;
    const mockValue = [{
      offsetHeight: 1000,
      setAttribute: jasmine.createSpy('setAttribute')
    }] as unknown as HTMLCollectionOf<Element>;
    spyOn(service.document, 'getElementsByClassName').and.returnValue(mockValue);
    spyOn(service, 'getGrid').and.returnValue(getMockHTMLElement(mockGridHTMLElementValue));
    spyOn(service, 'getFacetsContainerView').and.returnValue(getMockHTMLElement(mockFacetContainerHTMLElementValue));
    spyOn(service, 'getFacetsContainerFullView').and.returnValue(getMockHTMLElement(mockFacetContainerHTMLElementValue));
    const value = service.facetsScroll(true, data);
    expect(value.fixedFacetsContainer).toBeFalsy();
  });

  it('should return facetsScrollObj when facets space available and scroll reached facets section', () => {
    const data = { ...facetsScrollObj, filterBarTop: 30, maxTransition: 500 };
    const mockGridHTMLElementValue = { offsetHeight: 1500, scrollHeight: 1637 };
    const mockFacetContainerHTMLElementValue = { offsetHeight: 200 };
    spyOnProperty(document.documentElement, 'scrollTop', 'get').and.returnValue(400);
    (window['scrollY'] as any) = 80;
    (window['innerHeight'] as any) = 1400;
    const mockValue = [{
      offsetHeight: 1000,
      setAttribute: jasmine.createSpy('setAttribute')
    }] as unknown as HTMLCollectionOf<Element>;
    spyOn(service.document, 'getElementsByClassName').and.returnValue(mockValue);
    spyOn(service, 'getGrid').and.returnValue(getMockHTMLElement(mockGridHTMLElementValue));
    spyOn(service, 'getFacetsContainerView').and.returnValue(getMockHTMLElement(mockFacetContainerHTMLElementValue));
    spyOn(service, 'getFacetsContainerFullView').and.returnValue(getMockHTMLElement(mockFacetContainerHTMLElementValue));
    const value = service.facetsScroll(true, data);
    expect(value.fixedFacetsContainer).toBeTruthy();
  });

  it('should get return value when cleanData is called', () => {
    const oldItems = [{
      psid: '30001MHXH3AM/A',
      options: [{
        name: 'storage'
      }]
    }];
    const value = service.cleanData(oldItems);
    expect(value.length).toEqual(1);
  });

  it('should get psidSlug to be undefined when cleanData is called', () => {
    const oldItems = [{
      options: [{
        name: 'storage'
      }]
    }];
    const value = service.cleanData(oldItems);
    expect(value[0].psidSlug).toBeUndefined();
  });

  it('should get topFreezedElementHeight value equal to 112 when filterStickyTopOffset is called', () => {
    spyOn(notificationRibbonService, 'getCustomRibbonShow').and.returnValue(true);
    spyOn(notificationRibbonService, 'getCustomRibbonPersist').and.returnValue(false);
    const data = { ...facetsScrollObj };
    const value = service.filterStickyTopOffset(data);
    expect(value.topFreezedElementHeight).toEqual(112);
  });

  it('should get topFreezedElementHeight value equal to 60 when filterStickyTopOffset is called', () => {
    spyOn(notificationRibbonService, 'getCustomRibbonShow').and.returnValue(true);
    spyOn(notificationRibbonService, 'getCustomRibbonPersist').and.returnValue(false);
    service.config = {
      externalHeaderUrl: 'https://cep01.jpqa2.bridge2solutions.net/apple-sso/'
    };
    const data = { ...facetsScrollObj };
    const value = service.filterStickyTopOffset(data);
    expect(value.topFreezedElementHeight).toEqual(60);
  });

  it('should get topFreezedElementHeight value equal to 1698 when filterStickyTopOffset is called', () => {
    spyOn(notificationRibbonService, 'getCustomRibbonShow').and.returnValue(false);
    spyOn(notificationRibbonService, 'getCustomRibbonPersist').and.returnValue(true);
    service.config = {
      externalHeaderUrl: 'https://cep01.jpqa2.bridge2solutions.net/apple-sso/'
    };
    const mockHTMLCollectionOfElementValue = [{
      offsetHeight: 1056,
      style: {}
    }] as unknown as HTMLCollectionOf<Element>;
    spyOn(service.document, 'getElementsByClassName').and.returnValue(mockHTMLCollectionOfElementValue);
    const data = { ...facetsScrollObj };
    const value = service.filterStickyTopOffset(data);
    expect(value.topFreezedElementHeight).toEqual(60);
  });

  it('should get topFreezedElementHeight value equal to 60 when filterStickyTopOffset is called', () => {
    spyOn(notificationRibbonService, 'getCustomRibbonShow').and.returnValue(false);
    spyOn(notificationRibbonService, 'getCustomRibbonPersist').and.returnValue(true);
    service.config = {};
    const data = { ...facetsScrollObj };
    const value = service.filterStickyTopOffset(data);
    expect(value.topFreezedElementHeight).toEqual(60);
  });
});
