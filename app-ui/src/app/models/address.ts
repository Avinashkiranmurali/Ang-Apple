export interface Address {
  id?: number;
  firstName?: string;
  middleName?: string;
  lastName?: string;
  fullName?: string;
  businessName: string;
  address1: string;
  address2: string;
  address3?: string;
  subCity?: string;
  city: string;
  state: string;
  zip5: number;
  zip4?: string;
  country: string;
  phoneNumber: string;
  faxNumber?: string;
  version?: bigint;
  email: string;
  validAddress?: boolean;
  errorMessage?: Map<string, string>;
  warningMessage?: Map<string, string>;
  ignoreSuggestedAddress?: boolean | string;
  cartTotalModified?: boolean;
  selectedAddressId?: number;
  addressModified?: string;
  recipientName?: string;
  selectedAddressName?: string;
  addressName?: string;
  postalCode?: string;
  addressId?: number;
}

export interface ShippingAddress {
  firstName?: string;
  middleName?: string;
  businessName: string;
  address1: string;
  address2: string;
  address3?: string;
  city: string;
  state: string;
  postalCode?: string;
  country: string;

}
export interface BillTo {
  firstName?: string;
  lastName?: string;
  addressLine?: string;
  city?: string;
  state?: string;
  zip?: string;
  country?: string;
}
