import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ConfigOptionDirective } from './config-option.directive';
import { Renderer2, ElementRef, Component, Type, forwardRef, DebugElement } from '@angular/core';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { By } from '@angular/platform-browser';

// Simple test component that will not in the actual app
@Component({
    template: '<input appConfigOption [indexVal]="index" [optdata]="option" [config]="config" [messages]="messages" [optset]="optionSet">Testing Directives is awesome!'
})
class TestComponent {
    index = 1;
    option = {
        value: 'Silver',
        image: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/macbook-air-silver-select-201810_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1603332212000&qlt=95',
        key: 'silver',
        optDisable: false,
        optHidden: false,
        tabindex: 0,
        points: null
    };
    optionSet = {
        name: 'storage',
        title: 'Storage',
        optionData: [{
            value: '256GB',
            image: '',
            key: '256gb',
            optDisable: false,
            optHidden: false,
            tabindex: -1,
            points: null
        },
        {
            value: '512GB',
            image: '',
            key: '512gb',
            optDisable: false,
            optHidden: false,
            tabindex: -1,
            points: null
        }
        ],
        disabled: true,
        hidden: false,
        orderBy: 1,
        isDenomination: false
    };
    program = require('assets/mock/program.json');
    config = this.program['config'];
    messages = require('assets/mock/messages.json');
}

describe('ConfigOptionDirective', () => {
    let component: TestComponent;
    let fixture: ComponentFixture<TestComponent>;
    let directive: ConfigOptionDirective;
    let renderer2: Renderer2;
    let element: ElementRef;
    let input: DebugElement;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [
                TestComponent,
                ConfigOptionDirective
            ],
            providers: [
                { provide: CurrencyPipe },
                { provide: Renderer2 },
                { provide: ConfigOptionDirective },
                {
                    provide: NG_VALUE_ACCESSOR,
                    useExisting: forwardRef(() => ConfigOptionDirective),
                    multi: true
                }
            ]
        });
        fixture = TestBed.createComponent(TestComponent);
        component = fixture.componentInstance;
        element = fixture.debugElement;
        renderer2 = fixture.componentRef.injector.get<Renderer2>(Renderer2 as Type<Renderer2>);
        directive = new ConfigOptionDirective(element, renderer2);
        const index = 1;
        const optionData = {
            value: 'Silver',
            image: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/macbook-air-silver-select-201810_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1603332212000&qlt=95',
            key: 'silver',
            optDisable: false,
            optHidden: false,
            tabindex: 0,
            points: null
        };
        const optionSet = {
            name: 'storage',
            title: 'Storage',
            optionData: [{
                value: '256GB',
                image: '',
                key: '256gb',
                optDisable: false,
                optHidden: false,
                tabindex: -1,
                points: null
            },
            {
                value: '512GB',
                image: '',
                key: '512gb',
                optDisable: false,
                optHidden: false,
                tabindex: -1,
                points: null
            }
            ],
            disabled: true,
            hidden: false,
            orderBy: 1,
            isDenomination: false
        };
        const program = require('assets/mock/program.json');
        const config = program['config'];
        const messages = require('assets/mock/messages.json');
        directive.indexVal = index;
        directive.optset = optionSet;
        directive.optdata = optionData;
        directive.config = config;
        directive.messages = messages;
        input = fixture.debugElement.query(By.directive(ConfigOptionDirective));
        fixture.detectChanges();
    });

    it('should call an getconfigOption', () => {
        directive.optset.name = 'color';
        directive.optdata.key = 'product_red';
        directive.optdata.image = '';
        directive.getconfigOption();
    });

    it('should call an swatchImage for getconfigOption', () => {
        directive.optset.name = 'color';
        directive.optdata.image = 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/macbook-air-silver-select-201810_SW_COLOR?wid=32&hei=32&fmt=jpeg&qlt=80&op_usm=0.5,0.5&.v=1603332212000&qlt=95';
        directive.getconfigOption();
    });

    it('should call an bandColor for getconfigOption', () => {
        directive.optset.name = 'bandColor';
        directive.getconfigOption();
    });

    it('should execute and call getWatchImg method', () => {
        const locale = 'en_US';
        directive.width = 550;
        directive.getWatchImg('test');
        expect(directive.getWatchImg).toBeDefined();
    });

    it('should call getScreenSize', () => {
        directive.getScreenSize();
    });

    it('should call localeCountry', () => {
        expect(directive.localeCountry('en_UK')).toBeTruthy();
    });

    it('should call if for localeCountry', () => {
        expect(directive.localeCountry('es_MX')).toBeTruthy();
    });

    it('should execute and call else getWatchImg method', () => {
        (window['innerWidth'] as any) = 700;
        expect(directive.getWatchImg('test')).toBeTruthy();
    });

});
