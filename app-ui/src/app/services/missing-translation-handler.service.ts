import { Injectable } from '@angular/core';
import { MissingTranslationHandler, MissingTranslationHandlerParams } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class MissingTranslationHandlerService  implements MissingTranslationHandler {

  constructor() { }
  handle(params: MissingTranslationHandlerParams) {
    return '';
  }
}
