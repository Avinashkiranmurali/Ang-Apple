import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AcknowledgeTcModalComponent } from './acknowledge-tc-modal.component';
import {ModalsService} from '@app/components/modals/modals.service';
import {ActivatedRoute, Router} from '@angular/router';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {UserStoreService} from '@app/state/user-store.service';
import {NgbActiveModal, NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {CurrencyFormatPipe} from '@app/pipes/currency-format.pipe';
import {CurrencyPipe} from '@app/pipes/currency.pipe';
import {DecimalPipe} from '@angular/common';
import { TranslateServiceStub } from '@app/modules/shared/media-product/media-product.component.spec';

describe('AcknowledgeTcModalComponent', () => {
  let component: AcknowledgeTcModalComponent;
  let fixture: ComponentFixture<AcknowledgeTcModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AcknowledgeTcModalComponent ],
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

    fixture = TestBed.createComponent(AcknowledgeTcModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });
});
