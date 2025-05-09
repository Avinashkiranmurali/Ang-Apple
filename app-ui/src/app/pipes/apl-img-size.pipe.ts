import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'aplImgSize'
})
export class AplImgSizePipe implements PipeTransform {

  transform(value: string, width?: string, height?: string): string {
    width = (width !== undefined) ? width : '';
    height = (height !== undefined) ? height : width;
    value += (value?.indexOf('?') !== -1) ? '&qlt=95' : '?qlt=95';
    return value + '&wid=' + width + '&hei=' + height;
  }

}
