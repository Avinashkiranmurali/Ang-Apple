import { TestBed } from '@angular/core/testing';
import { FooterService } from '@app/modules/footer/footer.service';
import { DefaultFooterComponent } from '@app/components/vars/default/footer/default-footer.component';
import { RBCFooterComponent } from '@app/components/vars/rbc/footer/rbc-footer/rbc-footer.component';
import { RBCSimpleFooterComponent } from '@app/components/vars/rbc/footer/rbc-simple-footer/rbc-simple-footer.component';
import { UAFooterComponent } from '@app/components/vars/ua/footer/ua-footer.component';
import { ChaseFooterComponent } from '@app/components/vars/chase/footer/chase-footer.component';

describe('FooterService', () => {
  let service: FooterService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FooterService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('loadFooterComponent() to return respective template', () => {
    const footerTemplates = [
      { name : 'default', template: DefaultFooterComponent },
      { name : 'rbc', template: RBCFooterComponent },
      { name : 'rbc-simple', template: RBCSimpleFooterComponent },
      { name : 'ua', template: UAFooterComponent },
      { name : 'chase', template: ChaseFooterComponent }
    ];
    expect(service.loadFooterComponent('default')).toEqual(footerTemplates.find(object => object['name'] === 'default'));
  });
});
