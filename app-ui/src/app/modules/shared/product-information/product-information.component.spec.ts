import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ProductInformationComponent } from './product-information.component';
import { TranslateModule } from '@ngx-translate/core';
import { SafePipe } from '@app/pipes/safe.pipe';
import { AdditionalInfo } from '@app/models/additional-info';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { SharedService } from '@app/modules/shared/shared.service';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { NgbCollapseModule } from '@ng-bootstrap/ng-bootstrap';

describe('ProductInformationComponent', () => {
    let component: ProductInformationComponent;
    let fixture: ComponentFixture<ProductInformationComponent>;
    beforeEach(waitForAsync(() => {
        TestBed.configureTestingModule({
            declarations: [ProductInformationComponent, SafePipe],
            imports: [TranslateModule.forRoot(), BrowserAnimationsModule, NgbCollapseModule],
            providers: [
                {
                    provide: SharedService,
                    useValue: {
                        convertToRemUnit: () => {}
                    }
                },
                { provide: ActivatedRoute, useValue: {
                    params: of({ category: 'mac', subcat: 'macbook-pro', addcat: undefined }) }
                }
            ]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ProductInformationComponent);
        component = fixture.componentInstance;
        component.messages = require('assets/mock/messages.json');
        component.details = require('assets/mock/product-detail.json');
        component.pageTitle = '12.9-inch iPad Pro Wi‑Fi + Cellular 1TB - Space Gray';
        component.configItemSku = 'MXG22LL/A';
        component.configItemUpc = '190199455498';
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should call ngOnInit method', () => {
        (component as any).details = {};
        component.ngOnInit();
    });

    it('should call validateAdditionInfoData method', () => {
        spyOn(component, 'validateAdditionInfoData').and.callThrough();
        const additionalInfo: AdditionalInfo = component.details.additionalInfo[1];
        expect(component.validateAdditionInfoData(null)).toBe('');
        component.validateAdditionInfoData(additionalInfo);
        expect(component.validateAdditionInfoData).toHaveBeenCalled();
    });

    it('should call validateAdditionInfoData method for array and object dataTypes', () => {
        spyOn(component, 'validateAdditionInfoData').and.callThrough();
        const array = [
            'test', 'data', 'check'
        ];
        const objectData = {
            name: 'communication',
            value: 'Wi-Fi + Cellular',
            key: 'nocarrier',
            i18Name: 'Communication',
            orderBy: 0,
            points: null,
            swatchImageUrl: null
        };
        component.validateAdditionInfoData(array);
        component.validateAdditionInfoData(objectData);
        expect(component.validateAdditionInfoData).toHaveBeenCalledTimes(2);
    });

    it('should call objectKeys method', () => {
        spyOn(component, 'objectKeys').and.callThrough();
        component.objectKeys(component.details.merchantSpecificData);
        expect(component.objectKeys).toHaveBeenCalled();
    });

});
