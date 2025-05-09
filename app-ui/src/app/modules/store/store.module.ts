import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { StoreRoutingModule } from './store-routing.module';
import { CommonModule } from '@angular/common';
import { NgbActiveModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { StoreComponent } from './store.component';
import { NavComponent } from '@app/components/nav/nav.component';
import { SubNavComponent } from '@app/components/sub-nav/sub-nav.component';
import { FormsModule } from '@angular/forms';
import { UserDropdownComponent } from '@app/components/user-dropdown/user-dropdown.component';
import { SharedModule } from '@app/modules/shared/shared.module';
import { FooterModule } from '@app/modules/footer/footer.module';
import { HeaderModule } from '@app/modules/header/header.module';
import { SearchBoxComponent } from '@app/components/search-box/search-box.component';
import { QuickLinksComponent } from '@app/components/quick-links/quick-links.component';
import { MainNavComponent } from '@app/components/main-nav/main-nav.component';
import { CustomHoverLinkDirective } from '@app/directives/custom-hover-link.directive';
import { AnonModalComponent } from '@app/components/modals/anon/anon-modal.component';
import { BrowseOnlyComponent } from '@app/components/modals/browse-only/browse-only.component';
import { TimeoutComponent } from '@app/components/modals/timeout/timeout.component';
import { TimeoutWarningModelComponent } from '@app/components/modals/timeout-warning/timeout-warning-model.component';
import { AddressModalComponent } from '@app/components/modals/address-modal/address-modal.component';
import { OopsModalComponent } from '@app/components/modals/oops-modal/oops-modal.component';
import { ConsentFormComponent } from '@app/components/modals/consent-form/consent-form.component';
import { Title } from '@angular/platform-browser';
import { TransitionComponent } from '@app/transition/transition.component';
import { IdologyModelComponent } from '@app/components/modals/idology-model/idology-model.component';
import { NgIdleKeepaliveModule } from '@ng-idle/keepalive';
import { NotificationBannerComponent } from '@app/components/notification-banner/notification-banner.component';
import { IdologyLibModule } from '@bakkt/idology-lib';
import { EngraveModalComponent } from '@app/components/modals/engrave-modal/engrave-modal.component';
import { A11yModule } from '@angular/cdk/a11y';
import { PricingModule } from '@app/modules/pricing/pricing.module';

@NgModule({
  declarations: [
    StoreComponent,
    NavComponent,
    SubNavComponent,
    UserDropdownComponent,
    SearchBoxComponent,
    QuickLinksComponent,
    MainNavComponent,
    CustomHoverLinkDirective,
    AnonModalComponent,
    BrowseOnlyComponent,
    TimeoutComponent,
    TimeoutWarningModelComponent,
    AddressModalComponent,
    OopsModalComponent,
    ConsentFormComponent,
    TransitionComponent,
    IdologyModelComponent,
    NotificationBannerComponent,
    EngraveModalComponent
  ],
  imports: [
    SharedModule,
    FooterModule,
    HeaderModule,
    CommonModule,
    StoreRoutingModule,
    FormsModule,
    NgbModule,
    RouterModule,
    NgIdleKeepaliveModule.forRoot(),
    IdologyLibModule,
    PricingModule,
    A11yModule
  ],
  exports: [
    SharedModule,
    SearchBoxComponent
  ],
  providers: [
    Title,
    NgbActiveModal
  ]
})

// @ts-ignore
export class StoreModule {

}
