import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'parsePsid'
})
export class ParsePsidPipe implements PipeTransform {

  transform(item: string, value: string){
    return  item.replace(/[&\/\\#,+()$~%.'":*?<>{}]/g, value);
  }

}
