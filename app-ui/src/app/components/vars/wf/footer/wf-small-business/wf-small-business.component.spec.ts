import { ComponentFixture, TestBed } from '@angular/core/testing';
import { WFSmallBusinessFooterComponent } from './wf-small-business.component';
import { ModalsService } from '@app/components/modals/modals.service';

describe('WFSmallBusinessFooterComponent', () => {
  let component: WFSmallBusinessFooterComponent;
  let fixture: ComponentFixture<WFSmallBusinessFooterComponent>;
  let modalsService: ModalsService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WFSmallBusinessFooterComponent ],
      providers: [ ModalsService ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WFSmallBusinessFooterComponent);
    component = fixture.componentInstance;
    modalsService = TestBed.inject(ModalsService);
    fixture.detectChanges();
  });

});
