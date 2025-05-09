import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ErrorComponent } from './error.component';
import { SafePipe } from '@app/pipes/safe.pipe';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { PublicMessagesService } from '@app/services/public-messages.service';
import { of } from 'rxjs';
import { ActivatedRoute } from '@angular/router';

describe('ErrorComponent', () => {
  let component: ErrorComponent;
  let fixture: ComponentFixture<ErrorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ErrorComponent, SafePipe ],
      imports: [ HttpClientTestingModule ],
      providers: [
        {
          provide: PublicMessagesService,
          useValue: { getPublicMessages: () => of({ offerNoLongerValid: '<p> Sorry, this offer is no longer valid or has already been redeemed.</p>'}) }
        },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({ locale: 'en_CA' })
          }
        }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getPublicMessages method - when maintenance message exists', () => {
    spyOn(component, 'getPublicMessages').and.callThrough();
    spyOn(component['publicMessagesService'], 'getPublicMessages').and.returnValue(of({ offerNoLongerValid: '<p class="header">Check back soon</p><br/><p class="description">We’re busy updating new products for you. We’ll be back.</p>'}));
    component.getPublicMessages();
    expect(component.getPublicMessages).toHaveBeenCalled();
    expect(component.publicMessages.offerNoLongerValid).toBeDefined();
  });

  it('should call getPublicMessages method - when maintenance message doesnot exists', () => {
    spyOn(component, 'getPublicMessages').and.callThrough();
    const data = {
      offerNoLongerValid: null
    };
    spyOn(component['publicMessagesService'], 'getPublicMessages').and.returnValue(of(data));
    component.getPublicMessages();
    expect(component.getPublicMessages).toHaveBeenCalled();
    expect(component.publicMessages.offerNoLongerValid).toBeNull();
  });
});
