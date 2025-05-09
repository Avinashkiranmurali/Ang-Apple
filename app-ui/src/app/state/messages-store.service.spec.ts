import { TestBed } from '@angular/core/testing';
import { MessagesStoreService } from './messages-store.service';

describe('MessagesStoreService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: MessagesStoreService = TestBed.inject(MessagesStoreService);
    expect(service).toBeTruthy();
  });
});
