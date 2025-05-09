import { TestBed, ComponentFixture } from '@angular/core/testing';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { WFSpanishComponent } from './wf-footer-spanish-modal.component';

describe('WFSpanishComponent', () => {
  let component: WFSpanishComponent;
  let fixture: ComponentFixture<WFSpanishComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [WFSpanishComponent],
      providers: [
        { provide: MessagesStoreService },
        { provide: NgbActiveModal },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(WFSpanishComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
