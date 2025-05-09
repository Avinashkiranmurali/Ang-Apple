import { CategoryVarProgram } from '@app/models/category-var-program';
import { Product } from '@app/models/product';

export interface Category {
  imageUrl: string;
  i18nName: string;
  slug: string;
  name: string;
  templateType: string;
  defaultImage?: string;
  summaryIconImage: string;
  displayOrder: number;
  engraveBgImageLocation?: string;
  subCategories: Category[];
  parents: Category[];
  products: Product[];
  images: Map<string, string>;
  psid?: string;
  depth: number;
  categoryVarPrograms?: CategoryVarProgram[];
  supportedLocales?: string[];
  new: boolean;
  configurable: boolean;
  active: boolean;
  detailUrl?: string;
}
