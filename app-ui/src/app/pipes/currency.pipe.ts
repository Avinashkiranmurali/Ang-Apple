import { Pipe, PipeTransform } from '@angular/core';
import { formatCurrency, getCurrencySymbol } from '@angular/common';
import { UserStoreService } from '@app/state/user-store.service';

@Pipe({
  name: 'currency'
})
export class CurrencyPipe implements PipeTransform {

  constructor(
    private userStore: UserStoreService
    ) {}

  transform( value: number, currencyCode?: string, display?: 'symbol', digitsInfo?: string, locale?: string): string | null {
    if (!(this.userStore.config && this.userStore.config.useNarrowCurrencySymbol)) {
      return formatCurrency(
        value,
        this.userStore.user.locale,
        (this.userStore.config.currencySymbol !== undefined) ? this.userStore.config.currencySymbol : getCurrencySymbol(this.userStore.program.targetCurrency.code, 'wide'),
        this.userStore.program.targetCurrency.code,
        digitsInfo ? digitsInfo : '1.' + this.userStore.config.currencyDecimalPlaces + '-' + this.userStore.config.currencyDecimalPlaces,
      );
    } else {
      return formatCurrency(
        value,
        this.userStore.user.locale,
        (this.userStore.config.currencySymbol !== undefined) ? this.userStore.config.currencySymbol : getCurrencySymbol(this.userStore.program.targetCurrency.code, 'narrow'),
        this.userStore.program.targetCurrency.code,
        digitsInfo ? digitsInfo : '1.' + this.userStore.config.currencyDecimalPlaces + '-' + this.userStore.config.currencyDecimalPlaces,
      );
    }
  }
}
