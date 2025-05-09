import { Product } from '@app/models/product';
import { CategoryPrice } from '@app/models/filter-products';
import { Option } from '@app/models/option';

export interface ProductsWithConfiguration {
    products?: Product[];
    totalFound?: number;
    categoryPrices?: CategoryPrice[];
    optionsConfigurationData?: { [key: string]: Option};
    facetsFilters?: { [key: string]: Option};
    searchRedirect?: { [key: string]: string};
  }
