import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AplImgSizePipe } from '@app/pipes/apl-img-size.pipe';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { TranslateModule } from '@ngx-translate/core';
import { OrderTilesComponent } from './order-tiles.component';

describe('OrderTilesComponent', () => {
  let component: OrderTilesComponent;
  let fixture: ComponentFixture<OrderTilesComponent>;
  const orderHistoryData = require('assets/mock/order-history.json');
  const programData = require('assets/mock/program.json');
  const mockUser = require('assets/mock/user.json');
  const userData = {
    user: mockUser,
    program: programData,
    config: programData['config']
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        OrderTilesComponent,
        OrderByPipe,
        AplImgSizePipe
      ],
      imports: [
        HttpClientTestingModule,
        TranslateModule.forRoot(),
        RouterTestingModule,
      ],
      providers: [
        { provide: MessagesStoreService },
        { provide: UserStoreService, useValue: userData },
        AplImgSizePipe
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OrderTilesComponent);
    component = fixture.componentInstance;
    component.orderItems = orderHistoryData;
    component.messages = require('assets/mock/messages.json');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create medium for empty string', () => {
    component['userStore'].config.mediumDate = undefined;
    expect(component).toBeTruthy();
  });

  it('should create medium', () => {
    component['userStore'].config.mediumDate = 'd MMM y';
    expect(component).toBeTruthy();
  });

  it('should call ngOnInit method', () => {
    spyOn(component, 'ngOnInit').and.callThrough();
    component.orderItems = [];
    component.ngOnInit();
    expect(component.ngOnInit).toHaveBeenCalled();
  });

});
