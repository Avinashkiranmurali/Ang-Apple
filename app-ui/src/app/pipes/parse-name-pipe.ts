import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'parseName'
})
export class ParseNamePipe implements PipeTransform {

  transform(item: string){
    let parsed = item.replace(/\s/g, '');
    parsed = parsed.replace(/\-/g, '').replace(/"/g, '');
    const tempLng = parsed.length;
    if (tempLng > 10) {
      parsed = parsed.substring(0, 10);
    }
    return parsed;
  }

}
