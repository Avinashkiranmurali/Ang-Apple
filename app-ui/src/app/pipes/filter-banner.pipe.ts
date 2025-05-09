import { Pipe, PipeTransform } from '@angular/core';
import { Messages } from '@app/models/messages';

@Pipe({
  name: 'filterBanner'
})
export class FilterBannerPipe implements PipeTransform {

  transform(banners: Array<any>, messages: Messages): Array<any> {
    if (banners) {
      return banners.filter( banner => messages[banner['active']] === undefined || messages[banner['active']] === 'true');
    } else {
      return [];
    }
  }

}
