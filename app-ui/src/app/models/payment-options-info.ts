
import { Redemption } from '@app/models/redemption';

interface PaymentOptionsInfo {
  name: string;
  optionHeading: string;
  optionDescription: string;
  optionDescriptionPayment?: string;
  paySummarySubtitle: string;
  paySummaryTemplate: string;
  checked: boolean;
}

interface ActionPanel {
  nextStepBtnLabel: string;
  nextStep?: string[];
}

interface SplitPayOptions extends PaymentOptionsInfo {
  nextStepBtnLabel: string;
  isPaymentRequired?: boolean;
  isValid?: boolean;
  subView: { [key: string]: string };
}

export interface Payments extends PaymentOptionsInfo {
  value: string;
  isDisabled: boolean;
  actionPanel: ActionPanel;
  splitPayOptions: Map<string, SplitPayOptions>;
  redemptionOptions?: Map<string, Array<Redemption>>;
}
