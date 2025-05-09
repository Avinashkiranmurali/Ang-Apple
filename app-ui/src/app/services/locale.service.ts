import { Injectable } from '@angular/core';
import { registerLocaleData } from '@angular/common';

@Injectable({
  providedIn: 'root'
})

export class LocaleService {
  exceptionLocale = ['zh', 'th'];

  registerLocale(locale): void{
    let localeId;
    if (this.exceptionLocale.indexOf(locale.split('_')[0]) >= 0) {
      localeId = locale.substring(0, 2);
    } else {
      localeId = locale.replace('_', '-');
    }
    if (localeId !== 'en-US') {
      import(`/node_modules/@angular/common/locales/${localeId}.mjs`).then(module => registerLocaleData(module.default));
    }
  }
}
