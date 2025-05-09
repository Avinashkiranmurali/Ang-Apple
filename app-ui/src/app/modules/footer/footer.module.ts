import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RBCSimpleFooterComponent } from '@app/components/vars/rbc/footer/rbc-simple-footer/rbc-simple-footer.component';
import { RBCFooterComponent } from '@app/components/vars/rbc/footer/rbc-footer/rbc-footer.component';
import { FooterComponent } from '@app/modules/footer/footer.component';
import { FooterDirective } from '@app/modules/footer/footer.directive';
import { DefaultFooterComponent } from '@app/components/vars/default/footer/default-footer.component';
import { UAFooterComponent } from '@app/components/vars/ua/footer/ua-footer.component';
import { SharedModule } from '@app/modules/shared/shared.module';
import { ChaseFooterComponent } from '@app/components/vars/chase/footer/chase-footer.component';
import { RouterModule } from '@angular/router';


@NgModule({
  declarations: [
    FooterComponent,
    DefaultFooterComponent,
    RBCFooterComponent,
    RBCSimpleFooterComponent,
    UAFooterComponent,
    ChaseFooterComponent,
    FooterDirective
  ],
  imports: [
    SharedModule,
    CommonModule,
    RouterModule
  ],
  exports: [
    FooterComponent,
    DefaultFooterComponent,
    RBCFooterComponent,
    RBCSimpleFooterComponent,
    UAFooterComponent,
    ChaseFooterComponent,
    FooterDirective
  ],
  schemas: [
    CUSTOM_ELEMENTS_SCHEMA
  ]
})
export class FooterModule { }
