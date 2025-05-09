import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';
import { ShippingAddressRoutingModule } from '@app/modules/shipping-address/shipping-address-routing.module';
import { ShippingAddressComponent } from '@app/modules/shipping-address/shipping-address.component';
import { AddressFormEnComponent } from '@app/modules/shipping-address/address-form-en/address-form-en.component';
import { AddressFormAuComponent } from './address-form-au/address-form-au.component';
import { AddressFormAeComponent } from './address-form-ae/address-form-ae.component';
import { AddressFormHkComponent } from './address-form-hk/address-form-hk.component';
import { ShippingAddressFormDirective } from '@app/modules/shipping-address/shipping-address-form.directive';
import { SharedModule } from '@app/modules/shared/shared.module';
import { CustomSelectDirective } from '@app/modules/shipping-address/custom-select.directive';
import { ReactiveFormsModule } from '@angular/forms';
import { AddressFormSgComponent } from '@app/modules/shipping-address/address-form-sg/address-form-sg.component';
import { AddressFormTwComponent } from '@app/modules/shipping-address/address-form-tw/address-form-tw.component';
import { AddressFormMyComponent } from './address-form-my/address-form-my.component';
import { AddressFormThComponent } from './address-form-th/address-form-th.component';
import { AddressFormMxComponent } from './address-form-mx/address-form-mx.component';
import { AddressFormPhComponent } from './address-form-ph/address-form-ph.component';
import { DataMaskingModule } from '@bakkt/data-masking';


@NgModule({
  declarations: [
    ShippingAddressComponent,
    AddressFormEnComponent,
    AddressFormAuComponent,
    AddressFormAeComponent,
    AddressFormHkComponent,
    AddressFormMyComponent,
    ShippingAddressFormDirective,
    CustomSelectDirective,
    AddressFormSgComponent,
    AddressFormTwComponent,
    AddressFormThComponent,
    AddressFormMxComponent,
    AddressFormPhComponent
  ],
  imports: [
    CommonModule,
    ShippingAddressRoutingModule,
    ReactiveFormsModule,
    SharedModule,
    DataMaskingModule,
    NgbDropdownModule
  ]
})

export class ShippingAddressModule { }
