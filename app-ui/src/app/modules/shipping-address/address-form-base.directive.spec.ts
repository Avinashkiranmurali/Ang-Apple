import { AddressFormBaseDirective } from './address-form-base.directive';
import { FormGroup, FormControl } from '@angular/forms';

describe('AddressFormBaseDirective', () => {
  let addressFormBaseDirective: AddressFormBaseDirective;

  beforeEach(() => {
    addressFormBaseDirective = new AddressFormBaseDirective();
  });

  it('should create an instance', () => {
    expect(addressFormBaseDirective).toBeTruthy();
  });

  it('should match moreAddressLocales array length to 2', () => {
    expect(addressFormBaseDirective.moreAddressLocales.length).toEqual(2);
  });

  it('should return 0 when returnZero is called', () => {
    const value = addressFormBaseDirective.returnZero();
    expect(value).toBe(0);
  });

  it('should match form controls value equal to the mentioned string', () => {
    addressFormBaseDirective.changeShipAddress = new FormGroup({
        address1: new FormControl('address1'),
        address2: new FormControl('address2'),
        name: new FormControl('name'),
        city: new FormControl('city'),
    });
    const address1 = addressFormBaseDirective.address1Field.value;
    expect(address1).toEqual('address1');
    const address2 = addressFormBaseDirective.address2Field.value;
    expect(address2).toEqual('address2');
    const name = addressFormBaseDirective.nameField.value;
    expect(name).toEqual('name');
    const city = addressFormBaseDirective.cityField.value;
    expect(city).toEqual('city');
  });

  it('should call parentMultiAddressSelect.emit  when multiAddressSelect is called', () => {
    spyOn(addressFormBaseDirective.parentMultiAddressSelect, 'emit');
    addressFormBaseDirective.multiAddressSelect(1);
    expect(addressFormBaseDirective.parentMultiAddressSelect.emit).toHaveBeenCalled();
  });
});
