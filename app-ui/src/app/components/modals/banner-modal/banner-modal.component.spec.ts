import { ComponentFixture, TestBed } from '@angular/core/testing';
import {ModalsService} from '@app/components/modals/modals.service';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {NgbActiveModal, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {CurrencyFormatPipe} from '@app/pipes/currency-format.pipe';
import {CurrencyPipe} from '@app/pipes/currency.pipe';
import {DecimalPipe} from '@angular/common';
import { TranslateServiceStub } from '@app/modules/shared/media-product/media-product.component.spec';
import { BannerModalComponent } from './banner-modal.component';

describe('BannerModalComponent', () => {
  let component: BannerModalComponent;
  let fixture: ComponentFixture<BannerModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BannerModalComponent ],
      imports: [
        TranslateModule.forRoot(),
      ],
      providers: [
        { provide: ModalsService, useValue: {
            settop: () => ({})
          }
        },
        { provide: ActivatedRoute,
          useValue: { snapshot: { data: {pageName: 'CONFIRMATION'}}}
        },
        {
          provide: TranslateService,
          useClass: TranslateServiceStub
        },
        { provide: Router, useValue: {
            navigate: jasmine.createSpy('navigate') }
        },
        NgbModal,
        NgbActiveModal,
        CurrencyFormatPipe,
        CurrencyPipe,
        DecimalPipe
      ]
    })
      .compileComponents();

    fixture = TestBed.createComponent(BannerModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
