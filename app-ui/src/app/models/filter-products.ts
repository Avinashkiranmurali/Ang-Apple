import { Option } from '@app/models/option';
import { Product } from '@app/models/product';

export interface FilterProducts {
  products: Product[];
  totalFound: number;
  categoryPrices?: CategoryPrice[];
  optionsConfigurationData?: Map<string, Array<Option>>;
  facetsFilters: Map<string, Array<Option>>;
  searchRedirect?: any;
}

export interface FilterOption {
  disabled: boolean;
  i18Name: string;
  isFiltered: boolean;
  key: string;
  name: string;
  orderBy: number;
  points: number | null;
  swatchImageUrl: string | null;
  value: string;
}

export interface SortOptionsItems {
  label: string;
  sortBy: string | null;
  orderBy: string | null;
  key: string;
  hidden: boolean;
}

export interface SortOptions {
  [key: string]: SortOptionsItems;
}

export interface FilterProductsPayload {
  categorySlugs?: Array<string>;
  pageSize: number;
  resultOffSet: number;
  order: string;
  sort: string;
  promoTag?: string;
  facetsFilters?: {[key: string]: Array<FilterOption>} | {};
  withVariations?: boolean;
  keyword?: string;
}

export interface CategoryPrice {
  categoryName: string;
  option: Option;
  isFree: boolean;
  startingFromPrice: number;
  fromPriceMessage: string;
}
