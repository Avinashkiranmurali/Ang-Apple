import { OptionData } from '../models/optionData';

export interface OptionDataConfig {
  name: string;
  title: string;
  optionData: OptionData[];
  disabled: boolean;
  hidden: boolean;
  orderBy: number;
  isDenomination: boolean;
}
