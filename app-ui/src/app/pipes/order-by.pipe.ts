import { Pipe, PipeTransform } from '@angular/core';
import { orderBy } from 'lodash';
@Pipe({
  name: 'orderBy'
})
export class OrderByPipe implements PipeTransform {

  transform(value: any[], order = '', column: string = ''): any[] {
    if (!value || order === '' || !order) { return value; } // no array
    if (value.length <= 1) { return value; } // array with only one item
    if (!column || column === '') {
      if (order === 'asc') {
        return value.sort();
      } else if (order === 'numericOrder') {
        value.sort((a, b) => b.orderBy - a.orderBy);
        return value.sort().reverse();
      }else{
        return value.sort().reverse();
      }
    } // sort 1d array
    return orderBy(value, [column], [order]);
  }
}
