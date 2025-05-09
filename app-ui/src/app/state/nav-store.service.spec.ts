import { TestBed } from '@angular/core/testing';
import { NavStoreService } from './nav-store.service';

describe('NavStoreService', () => {
  let service: NavStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NavStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
