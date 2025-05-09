import { Injectable } from '@angular/core';
import { DefaultHeaderComponent } from '@app/components/vars/default/header/default-header.component';
import { ChaseHeaderComponent } from '@app/components/vars/chase/header/chase-header.component';

@Injectable({
  providedIn: 'root'
})
export class HeaderService {

  headerTemplates: Array<object> = [
    { name : 'default', template: DefaultHeaderComponent },
    { name : 'chase', template: ChaseHeaderComponent }
  ];
  constructor() { }

  loadHeaderComponent(name): object {
   return this.headerTemplates.find(object => object['name'] === name);
  }
}
