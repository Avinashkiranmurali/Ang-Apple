import { ComponentFixture, TestBed } from '@angular/core/testing';
import { WFFooterComponent } from './wf-footer.component';
import { ModalsService } from '@app/components/modals/modals.service';

describe('WFFooterComponent', () => {
  let component: WFFooterComponent;
  let fixture: ComponentFixture<WFFooterComponent>;
  let modalsService: ModalsService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ WFFooterComponent ],
      providers: [ ModalsService ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WFFooterComponent);
    component = fixture.componentInstance;
    modalsService = TestBed.inject(ModalsService);
    fixture.detectChanges();
  });

});
