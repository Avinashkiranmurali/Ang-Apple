import { Pipe, PipeTransform } from '@angular/core';
import { formatNumber } from '@angular/common';
import { UserStoreService } from '@app/state/user-store.service';
import { User } from '@app/models/user';

@Pipe({
  name: 'number'
})
export class NumberPipe implements PipeTransform {
  user: User;

  constructor(
      private userStore: UserStoreService
    ) {
      this.user = this.userStore.user;
    }

  transform(value: any, locale?: string): string {
    return formatNumber(value, this.user.locale);
  }

}
