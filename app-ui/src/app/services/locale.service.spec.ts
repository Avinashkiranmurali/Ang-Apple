import { TestBed } from '@angular/core/testing';
import { LocaleService } from '@app/services/locale.service';
import { DataMaskingModule } from '@bakkt/data-masking';

describe('LocaleService', () => {
  let localeService: LocaleService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [],
      imports: [
        DataMaskingModule
      ]
    });
    localeService = TestBed.inject(LocaleService);
  });

  it('should be created', () => {
    expect(localeService).toBeTruthy();
  });

  it('should call registerLocale Method for en_US Locale', () => {
    spyOn(localeService, 'registerLocale').and.callThrough();
    const locale = 'en_US';
    localeService.registerLocale(locale);
    expect(localeService.registerLocale).toHaveBeenCalled();
  });

  it('should call registerLocale Method with non-US locale when exceptionLocale is involved', () => {
    spyOn(localeService, 'registerLocale').and.callThrough();
    const locale = 'zh_HK';
    localeService.registerLocale(locale);
    expect(localeService.registerLocale).toHaveBeenCalled();
  });

});
