import { Directive, EventEmitter , Input, Output } from '@angular/core';
import { FormGroup } from '@angular/forms';

import { Address } from '@app/models/address';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';

@Directive()
export class AddressFormBaseDirective {

    moreAddressLocales: Array<string>;

    @Input() changeShipAddress: FormGroup;
    @Input() errorMessage: {[key: string]: string};
    @Input() user: User;
    @Input() isMultiAddress: boolean;
    @Input() extras: {[key: string]: boolean};
    @Input() overrideShipping: boolean;
    @Input() shipAddress: Address;
    @Input() config: Config;
    @Input() isPostalCode: boolean;
    @Input() zipMaxLength: number;
    @Input() phoneMaxLength: number;

    @Output() parentMultiAddressSelect: EventEmitter<number | null> = new EventEmitter<number|null>();

    constructor() {
        this.moreAddressLocales = ['en_gb', 'en_za'];
    }

    returnZero() {
        return 0;
    }

    multiAddressSelect(addressId: number | null) {
        this.parentMultiAddressSelect.emit(addressId);
    }

    get address1Field() {
        return this.changeShipAddress.get('address1');
    }

    get address2Field() {
        return this.changeShipAddress.get('address2');
    }

    get nameField() {
        return this.changeShipAddress.get('name');
    }

    get cityField() {
        return this.changeShipAddress.get('city');
    }
}
