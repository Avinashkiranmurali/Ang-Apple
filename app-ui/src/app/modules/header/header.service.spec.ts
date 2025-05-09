import { TestBed } from '@angular/core/testing';
import { HeaderService } from './header.service';
import { DefaultHeaderComponent } from '@app/components/vars/default/header/default-header.component';
import { ChaseHeaderComponent } from '@app/components/vars/chase/header/chase-header.component';


describe('HeaderService', () => {
  let headerService: HeaderService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    headerService = TestBed.inject(HeaderService);
  });

  it('should be created', () => {
    expect(headerService).toBeTruthy();
  });

  it('loadHeaderComponent() to return respective template', () => {
    const headerTemplates = [
      { name : 'default', template: DefaultHeaderComponent },
      { name : 'chase', template: ChaseHeaderComponent }
    ];
    expect(headerService.loadHeaderComponent('default')).toEqual(headerTemplates.find(object => object['name'] === 'default'));
  });
});
