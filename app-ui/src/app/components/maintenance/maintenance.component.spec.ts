import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { PublicMessagesService } from '@app/services/public-messages.service';
import { SafePipe } from '@app/pipes/safe.pipe';
import { MaintenanceComponent } from './maintenance.component';
import { of } from 'rxjs';

describe('MaintenanceComponent', () => {
  let component: MaintenanceComponent;
  let fixture: ComponentFixture<MaintenanceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MaintenanceComponent, SafePipe, ],
      imports: [ HttpClientTestingModule ],
      providers: [
        {
          provide: PublicMessagesService,
          useValue: { getPublicMessages: () => of({ maintenanceMessage: '<p class="header">Check back soon</p><br/><p class="description">We’re busy updating new products for you. We’ll be back.</p>'}) }
        },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({ locale: 'en_US' })
          }
        }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MaintenanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call getPublicMessages method - when maintenance message exists', () => {
    spyOn(component, 'getPublicMessages').and.callThrough();
    spyOn(component['publicMessagesService'], 'getPublicMessages').and.returnValue(of({ maintenanceMessage: '<p class="header">Check back soon</p><br/><p class="description">We’re busy updating new products for you. We’ll be back.</p>'}));
    component.getPublicMessages();
    expect(component.getPublicMessages).toHaveBeenCalled();
    expect(component.publicMessages.maintenanceMessage).toBeDefined();
  });

  it('should call getPublicMessages method - when maintenance message doesnot exists', () => {
    spyOn(component, 'getPublicMessages').and.callThrough();
    const data = {
      maintenanceMessage: null
    };
    spyOn(component['publicMessagesService'], 'getPublicMessages').and.returnValue(of(data));
    component.getPublicMessages();
    expect(component.getPublicMessages).toHaveBeenCalled();
    expect(component.publicMessages.maintenanceMessage).toBeNull();
  });

});
