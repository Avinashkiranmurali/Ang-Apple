import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SharedModule } from '@app/modules/shared/shared.module';
import { HeaderDirective } from '@app/modules/header/header.directive';
import { HeaderComponent } from '@app/modules/header/header.component';
import { WelcomeMsgComponent } from '@app/components/vars/default/header/welcome-msg/welcome-msg.component';
import { DefaultHeaderComponent } from '@app/components/vars/default/header/default-header.component';
import { LogoComponent } from '@app/components/vars/default/header/logo/logo.component';
import { ChaseHeaderComponent } from '@app/components/vars/chase/header/chase-header.component';
import { PromoBannerComponent } from '@app/components/vars/chase/header/promo-banner/promo-banner.component';

@NgModule({
  declarations: [
    HeaderComponent,
    LogoComponent,
    WelcomeMsgComponent,
    DefaultHeaderComponent,
    ChaseHeaderComponent,
    PromoBannerComponent,
    HeaderDirective
  ],
  imports: [
    CommonModule,
    SharedModule
  ],
  exports: [
    HeaderComponent,
    LogoComponent,
    WelcomeMsgComponent,
    DefaultHeaderComponent,
    ChaseHeaderComponent,
    PromoBannerComponent,
    HeaderDirective
  ],
  schemas: [
    CUSTOM_ELEMENTS_SCHEMA
  ]
})
export class HeaderModule { }
