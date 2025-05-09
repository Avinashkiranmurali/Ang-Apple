import { Pipe, PipeTransform } from '@angular/core';
import { formatCurrency, formatNumber, getCurrencySymbol } from '@angular/common';
import { UserStoreService } from '@app/state/user-store.service';

@Pipe({
  name: 'currencyFormat'
})
export class CurrencyFormatPipe implements PipeTransform {

  constructor(
    private userStore: UserStoreService
  ) {}

  transform(value: number, format: string, locale: string, currencyCode?: string, display?: 'symbol', digitsInfo?: string): string | null {
    if (format === 'points_decimal') {
      if (!(this.userStore.config && this.userStore.config.useNarrowCurrencySymbol)) {
        return formatCurrency(
          value / 100,
          this.userStore.user.locale,
          (this.userStore.config.currencySymbol !== undefined) ? this.userStore.config.currencySymbol : getCurrencySymbol(this.userStore.program.targetCurrency.code, 'wide'),
          this.userStore.program.targetCurrency.code,
          digitsInfo ? digitsInfo : '1.' + this.userStore.config.currencyDecimalPlaces + '-' + this.userStore.config.currencyDecimalPlaces,
        );
      } else {
        return formatCurrency(
          value / 100,
          this.userStore.user.locale,
          (this.userStore.config.currencySymbol !== undefined) ? this.userStore.config.currencySymbol : getCurrencySymbol(this.userStore.program.targetCurrency.code, 'narrow'),
          this.userStore.program.targetCurrency.code,
          digitsInfo ? digitsInfo : '1.' + this.userStore.config.currencyDecimalPlaces + '-' + this.userStore.config.currencyDecimalPlaces,
        );
      }
      return formatCurrency(value / 100, locale, (this.userStore.config.currencySymbol !== undefined) ? this.userStore.config.currencySymbol : '$');
    } else {
      return formatNumber(value, locale);
    }
  }

}
