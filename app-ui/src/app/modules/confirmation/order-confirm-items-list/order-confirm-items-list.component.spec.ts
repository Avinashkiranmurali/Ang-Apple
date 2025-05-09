import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { OrderConfirmItemsListComponent } from './order-confirm-items-list.component';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { TemplateService } from '@app/services/template.service';
import { UserStoreService } from '@app/state/user-store.service';

describe('OrderConfirmItemsListComponent', () => {
  let component: OrderConfirmItemsListComponent;
  let fixture: ComponentFixture<OrderConfirmItemsListComponent>;
  const userData = {
    user : {
      locale: 'en_US'
    },
    config : {
      loginRequired : false,
      SFProWebFont : true
    }
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ OrderConfirmItemsListComponent, OrderByPipe ],
      providers: [
        MessagesStoreService,
        {
          provide: TemplateService,
          useValue: {
            getTemplatesProperty: () => ({
                orderConfirmItemsList: {
                  template: 'cart-subsidy.htm'
                }
              })
          }
        },
        { provide: UserStoreService, useValue : userData }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(OrderConfirmItemsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
