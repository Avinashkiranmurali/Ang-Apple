import { Injectable } from '@angular/core';
import { DefaultFooterComponent } from '@app/components/vars/default/footer/default-footer.component';
import { RBCFooterComponent } from '@app/components/vars/rbc/footer/rbc-footer/rbc-footer.component';
import { RBCSimpleFooterComponent } from '@app/components/vars/rbc/footer/rbc-simple-footer/rbc-simple-footer.component';
import { UAFooterComponent } from '@app/components/vars/ua/footer/ua-footer.component';
import { ChaseFooterComponent } from '@app/components/vars/chase/footer/chase-footer.component';
import { WFFooterComponent } from '@app/components/vars/wf/footer/wf-consumer/wf-footer.component';
import { WFSmallBusinessFooterComponent } from '@app/components/vars/wf/footer/wf-small-business/wf-small-business.component';

@Injectable({
  providedIn: 'root'
})
export class FooterService {

  footerTemplates: Array<object> = [
    { name : 'default', template: DefaultFooterComponent },
    { name : 'rbc', template: RBCFooterComponent },
    { name : 'rbc-simple', template: RBCSimpleFooterComponent },
    { name : 'ua', template: UAFooterComponent },
    { name : 'chase', template: ChaseFooterComponent },
    { name : 'wf-consumer', template: WFFooterComponent },
    { name : 'wf-small-business', template: WFSmallBusinessFooterComponent }
  ];
  constructor() { }

  loadFooterComponent(name): object {
   return this.footerTemplates.find(object => object['name'] === name);
  }

}
