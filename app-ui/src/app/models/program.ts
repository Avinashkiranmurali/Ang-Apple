import { Payment } from '@app/models/payment';
import { TargetCurrency } from '@app/models/target-currency';
import { Config } from '@app/models/config';
import { Redemption } from '@app/models/redemption';

export interface Program {
  varId: string;
  programId: string;
  name: string;
  imageUrl: string;
  convRate: number;
  pointName: string;
  formatPointName: string;
  pointPurchaseRate?: number;
  pointFormat?: string;
  isDemo: boolean;
  isActive: boolean;
  isLocal: boolean;
  catalogId: string;
  bundledPricingOption: string;
  payments: Array<Payment>;
  pricingTier: string;
  ccFilters?: string;
  pricingModels: Array<object>;
  redemptionOptions: Map<string, Array<Redemption>>;
  config: Config;
  targetCurrency: TargetCurrency;
  categoryPrices?: Array<object>;
  skipAddressValidation: boolean;
  programEligibleForPayrollDeduction: boolean;
  sessionConfig: {
    buildId: string;
    imageServerBuildNumber: string;
    five9Config: {
      chatEnabled: boolean;
      profile: string;
      rootUrl: string;
      tenant: string;
      title: string;
      type: string;
    };
  };
  carouselPages: Array<string>;
}
