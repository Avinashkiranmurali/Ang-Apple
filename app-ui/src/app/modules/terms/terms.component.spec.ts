import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { TermsComponent } from './terms.component';
import { TemplateStoreService } from '@app/state/template-store.service';
import { TranslateModule } from '@ngx-translate/core';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { EnsightenService } from '@app/analytics/ensighten/ensighten.service';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { DecimalPipe } from '@angular/common';

describe('TermsComponent with ActivatedRouter entries', () => {
  let component: TermsComponent;
  let fixture: ComponentFixture<TermsComponent>;
  let templateStoreService: TemplateStoreService;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TermsComponent ],
      imports: [
        TranslateModule.forRoot(),
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        {
          provide: EnsightenService, useValue: {
            broadcastEvent: () => {}
          }
        },
        { provide: ActivatedRoute, useValue: {
          snapshot: {
            data: {
              brcrumb: 'Terms',
              theme: 'main-terms',
              pageName: 'TERMS',
              analyticsObj: {
                pgName: 'apple_products:store_policies',
                pgType: 'admin',
                pgSectionType: 'information'
              }
            }
          },
          fragment: of('SalesTandC')
          }
        },
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    })
    .compileComponents();
    templateStoreService = TestBed.inject(TemplateStoreService);
    const configData = require('assets/mock/configData.json');
    templateStoreService.addTemplate(configData['configData']);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TermsComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.bodyTemplate = {};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});

describe('TermsComponent without ActivatedRouter entries', () => {
  let component: TermsComponent;
  let fixture: ComponentFixture<TermsComponent>;
  let templateStoreService: TemplateStoreService;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ TermsComponent ],
      imports: [
        TranslateModule.forRoot(),
        RouterTestingModule,
        HttpClientTestingModule
      ],
      providers: [
        {
          provide: EnsightenService, useValue: {
            broadcastEvent: () => {}
          }
        },
        { provide: ActivatedRoute, useValue: {
          snapshot: {
            data: {
              brcrumb: 'Terms',
              theme: 'main-terms',
              pageName: 'TERMS',
              analyticsObj: null
            }
          },
          fragment: of('')
         }
        },
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    })
    .compileComponents();
    templateStoreService = TestBed.inject(TemplateStoreService);
    const configData = require('assets/mock/configData.json');
    templateStoreService.addTemplate(configData['configData']);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TermsComponent);
    component = fixture.componentInstance;
    component.messages = require('assets/mock/messages.json');
    component.bodyTemplate = {};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
