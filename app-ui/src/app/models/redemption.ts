export interface Redemption {
  id: number;
  varId: string;
  programId: string;
  paymentOption: string;
  limitType: string;
  paymentMinLimit: number;
  paymentMaxLimit: number;
  orderBy: number;
  paymentProvider?: string;
  lastUpdatedBy: string;
  lastUpdatedDate: number;
  active: boolean;
}
