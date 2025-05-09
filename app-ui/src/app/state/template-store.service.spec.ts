import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { TemplateStoreService } from './template-store.service';

describe('TemplateStoreService', () => {
  let templateStoreService: TemplateStoreService;
  const mockTemplate = require('assets/mock/configData.json');

  beforeEach(() => {
    TestBed.configureTestingModule({});
    templateStoreService = TestBed.inject(TemplateStoreService);
    templateStoreService.template = mockTemplate['configData'];
  });

  afterAll(() => {
    templateStoreService.template = mockTemplate['configData'];
  });

  it('should be created', () => {
    expect(templateStoreService).toBeTruthy();
  });

  it('should call addTemplate Method', () => {
    spyOn(templateStoreService, 'addTemplate').and.callThrough();
    const data = require('assets/mock/configData.json');
    templateStoreService.addTemplate(data);
    expect(templateStoreService.addTemplate).toHaveBeenCalled();
  });

  it('should get detailsTemplate value', () => {
    expect(templateStoreService.detailsTemplate).toEqual(mockTemplate['configData']['templates']['detail']);
  });

  it('should get cartTemplate value', () => {
    expect(templateStoreService.cartTemplate).toEqual(mockTemplate['configData']['templates']['cart']);
  });

  it('should get footerTemplate value', () => {
    expect(templateStoreService.footerTemplate).toEqual(mockTemplate['configData']['templates']['footer']);
  });

  it('should get empty object when footerTemplate is not available', () => {
    templateStoreService.template = {
      templates: {}
    };
    expect(templateStoreService.footerTemplate).toEqual({});
  });

  it('should get checkoutTemplate value', () => {
    expect(templateStoreService.checkoutTemplate).toEqual(mockTemplate['configData']['templates']['checkout']);
  });

  it('should get orderDetailTemplate value', () => {
    expect(templateStoreService.orderDetailTemplate).toEqual(mockTemplate['configData']['templates']['orderStatus']);
  });

  it('should get orderHistoryTemplate value', () => {
    expect(templateStoreService.orderHistoryTemplate).toEqual(mockTemplate['configData']['templates']['orderHistory']);
  });

  it('should get orderConfirmationTemplate value', () => {
    expect(templateStoreService.orderConfirmationTemplate).toEqual(mockTemplate['configData']['templates']['orderConfirmation']);
  });

  it('should get userDropdownLinks value', () => {
    expect(templateStoreService.userDropdownLinks).toEqual(mockTemplate['configData']['templates']['userDropdownLinks']);
  });

  it('should get headerTemplate value', () => {
    expect(templateStoreService.headerTemplate).toEqual(mockTemplate['configData']['templates']['header']);
  });

  it('should get anonymousModal value', () => {
    expect(templateStoreService.anonymousModal).toBeTruthy();
  });

  it('should get anonymousModal value if exists', () => {
    templateStoreService.template = {
      templates: {
        anonymousModal: {
          size: 'lg',
          className: 'error-modal'
        }
      }
    };
    expect(templateStoreService.anonymousModal).toBeTruthy();
  });

  it('should get navigationTemplate value', () => {
    expect(templateStoreService.navigationTemplate).toEqual(mockTemplate['configData']['templates']['navigation']);
  });

  it('should get bodyTemplate value', () => {
    expect(templateStoreService.bodyTemplate).toEqual(mockTemplate['configData']['templates']['body']);
  });

  it('should get buttonColor value', () => {
    expect(templateStoreService.buttonColor).toBeNull();
  });

});
