import { TestBed } from '@angular/core/testing';

import { EncryptDecryptService } from './encrypt-decrypt.service';

describe('EncryptDecryptServiceService', () => {
  let service: EncryptDecryptService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EncryptDecryptService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
