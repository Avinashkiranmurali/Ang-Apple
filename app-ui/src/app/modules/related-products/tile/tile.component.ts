import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Product } from '@app/models/product';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { User } from '@app/models/user';
import { UserStoreService } from '@app/state/user-store.service';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-tile',
  templateUrl: './tile.component.html',
  styleUrls: ['./tile.component.scss']
})

export class TileComponent implements OnInit {

  @Input() item: Product;
  @Input() messages: Messages;
  @Input() index: number;
  @Output() addTileItemToCart = new EventEmitter<any>();
  @Output() getVariationDetails = new EventEmitter<any>();

  config: Config;
  user: User;
  productByConfigOptions: any = {};
  itemOptions: Array<any> = [];
  userProductsFilterBySelections: Product[];
  selectedVariant: object = {};
  hoverColor: string;
  allOptionsSelected: boolean;
  disabled: boolean;
  selectedVariantText: string;

  constructor(
    private userStore: UserStoreService,
    public sharedService: SharedService
  ) {
    this.config = this.userStore.config;
    this.user = this.userStore.user;
    this.itemOptions = [];
    this.allOptionsSelected = true;
  }

  ngOnInit(): void {
    const productDetail = this.item;
    if (this.item.optionsConfigurationData && Object.keys(productDetail.optionsConfigurationData).length > 0) {

      this.itemOptions = this.itemOptions.length === 0 ? this.sharedService.transformOptions(productDetail.optionsConfigurationData, '', false) : this.itemOptions;

      if (this.itemOptions.length > 0) {
        this.itemOptions[0].optionData = this.itemOptions[0].optionData.sort((a, b) => (a.value === b.value) ? 1 : -1);
        if (this.itemOptions) {
          this.itemOptions[0].optionData = this.sharedService.getProductRedValue(this.itemOptions[0].optionData, ['(product)red', 'product_red']);
        }
      }

      const nameVal = {};
      for (const key of Object.keys(productDetail.options)) {
        const obj = productDetail.options[key];
        nameVal[obj['name']] = obj['key'];
        this.selectedVariantText = obj['value'];
      }
      this.selectedVariant = nameVal;
      this.sharedService.constructDependedOptions(this.itemOptions, this.productByConfigOptions);
      this.productFindingByConfigOptions(this.item.variations); // configProducts
    }
    this.disabled = false;
  }

  productFindingByConfigOptions(configProducts) {
    // Set filtered products
    this.setFilteredProducts(configProducts);
    // 1. Loop the Product List
    for (const product of configProducts) {
      // 2. Loop the options List inside the product
      for (const configOption of product.options) {
        // 3. Define new product option key if not initiated and assign the product
        const key = configOption.key.split(' ').join('');
        if (!this.productByConfigOptions[configOption.name]) {
          continue;
        }
        this.productByConfigOptions[configOption.name].options[key].hidden = false;
        // minProduct configuration not required in details component
        // 4.1 config selection exist then find the product options satisfied with the selection and compare
        if (Object.keys(this.selectedVariant).length > 0) {
          const productOptionMatched = this.sharedService.verifyProductOptionOnProduct(this.selectedVariant, product, this.productByConfigOptions[configOption.name].options[key].dependedOptions);
          this.productByConfigOptions[configOption.name].options[key].hidden = true;
          if (productOptionMatched) {
            this.productByConfigOptions[configOption.name].options[key].hidden = false;
          }
        }
      }
    }
  }
  setFilteredProducts(configProducts){
    this.userProductsFilterBySelections = [...configProducts];
    let enableNextOption = true;
    let disableAllNextOption = false;
    let allOptionSelected = true;
    for (const option in this.productByConfigOptions){
      if (option){
        this.productByConfigOptions[option].filteredProducts = [];
        if (enableNextOption){
          this.productByConfigOptions[option].disabled = !enableNextOption;
        }else{
          delete this.selectedVariant[option];
          allOptionSelected = false;
        }
        if (disableAllNextOption){
          allOptionSelected = false;
          this.productByConfigOptions[option].disabled = true;
        }
        // Set Filtered Products and enable next options
        enableNextOption = false;
        if (this.selectedVariant[option]){ // Selected key and Value
          this.userProductsFilterBySelections = this.sharedService.filterProducts(this.userProductsFilterBySelections, this.selectedVariant[option], option);
          this.productByConfigOptions[option].filteredProducts = this.userProductsFilterBySelections;
          if (this.userProductsFilterBySelections.length > 0){
            enableNextOption = true;
          }else{
            allOptionSelected = false;
            disableAllNextOption = true;
            delete this.selectedVariant[option];
          }
        }else{
          allOptionSelected = false;
        }
      }
    }
    this.allOptionsSelected = allOptionSelected;
  }

  changeVariantOption(optionObj, optionSet, isInitialState?) {
    this.selectedVariant[optionSet['name']] = optionObj;
    if (!isInitialState) { // Should be called only on change event
      this.disabled = true;
      this.productFindingByConfigOptions(this.item.variations);
      this.item = this.userProductsFilterBySelections[0];
      this.selectedVariantText = this.item.options[0].value;
      this.getVariationDetails.emit({psid: this.userProductsFilterBySelections[0].psid, index: this.index});
      setTimeout(() => {
          const selectedVariantElement: HTMLElement = document.querySelector('.selected');
          selectedVariantElement?.focus();
        }, 2000);
    }
  }
  addItemToCart(psid: string, detailProduct: Product) {
    this.addTileItemToCart.emit({psid, detailProduct});
  }
  mouseHoverEvent(hoverValue: string) {
    this.hoverColor = hoverValue;
  }
  mouseLeaveEvent() {
    this.hoverColor = '';
  }
}
