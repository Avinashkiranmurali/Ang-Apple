import { Pipe, PipeTransform } from '@angular/core';
import * as _ from 'lodash';

@Pipe({
  name: 'interpolate'
})

export class InterpolatePipe implements PipeTransform {

  transform(value: string, key: string, data: object, conditionObj?: object): string {

    // if-condition is to execute the JS expression in interpolation
    const preValue = (/\{\{(.+?)\}\}/g.exec(value)) ? /\{\{(.+?)\}\}/g.exec(value)[1] : '';
    if (preValue.indexOf('?') > -1) {
      const experession = preValue.split('?')[1].split(':');
      const condition = ( preValue.split('?')[0].indexOf('(') > -1 ) ? preValue.split('?')[0].trim().slice(1, -1) : preValue.split('?')[0].trim();
      return value.replace(/\{\{(.+?)\}\}/g, (match, p1) => conditionObj[condition] ? data[experession[0].trim()] : data[experession[1].trim()]);
    } else {
      _.templateSettings = {
        interpolate: /\{\{(.+?)\}\}/g
      };

      const compiled = _.template( value );
      return  compiled( { [key] : data } );
    }
  }

}
