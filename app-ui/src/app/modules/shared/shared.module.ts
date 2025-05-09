import { NgModule } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { InterpolatePipe } from '@app/pipes/interpolate.pipe';
import { FilterBannerPipe } from '@app/pipes/filter-banner.pipe';
import { OrderByPipe } from '@app/pipes/order-by.pipe';
import { SafePipe } from '@app/pipes/safe.pipe';
import { CurrencyFormatPipe } from '@app/pipes/currency-format.pipe';
import { MissingTranslationHandler, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { CustomTranslateLoaderService } from '@app/services/custom-translate-loader.service';
import { ProductInformationComponent } from '@app/modules/shared/product-information/product-information.component';
import { PageTitleComponent } from '@app/modules/shared/page-title/page-title.component';
import { ConfigOptionDirective } from '@app/directives/config-option.directive';
import { CategoryNavigationTrayComponent } from '@app/modules/shared/category-navigation-tray/category-navigation-tray.component';
import { FormsModule } from '@angular/forms';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { RouterModule } from '@angular/router';
import { ParseNamePipe } from '@app/pipes/parse-name-pipe';
import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';
import { MissingTranslationHandlerService } from '@app/services/missing-translation-handler.service';
import { NumberPipe } from '@app/pipes/number.pipe';
import { MediaProductComponent } from '@app/modules/shared/media-product/media-product.component';
import { MediaProductModalComponent } from '@app/modules/shared/media-product-modal/media-product-modal.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { CustomFontScaleDirective } from '@app/modules/landing/banners/column-grid-banner/custom-font-scale.directive';
import { SwatchesColorComponent } from '@app/modules/shared/swatches-color/swatches-color.component';
import { AplImgSizePipe } from '@app/pipes/apl-img-size.pipe';
import { SmartPriceComponent } from '@app/modules/shared/smart-price/smart-price.component';

@NgModule({
  declarations: [
    InterpolatePipe,
    FilterBannerPipe,
    ConfigOptionDirective,
    OrderByPipe,
    SafePipe,
    CurrencyFormatPipe,
    ProductInformationComponent,
    PageTitleComponent,
    CategoryNavigationTrayComponent,
    CurrencyPipe,
    ParseNamePipe,
    ParsePsidPipe,
    NumberPipe,
    MediaProductComponent,
    MediaProductModalComponent,
    CustomFontScaleDirective,
    SwatchesColorComponent,
    AplImgSizePipe,
    SmartPriceComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    NgbModule,
    TranslateModule.forChild({
      loader: {
        provide: TranslateLoader,
        useClass: CustomTranslateLoaderService
      },
      missingTranslationHandler: {
        provide: MissingTranslationHandler,
        useClass: MissingTranslationHandlerService
      }
    })
  ],
  exports: [
    InterpolatePipe,
    FilterBannerPipe,
    ConfigOptionDirective,
    OrderByPipe,
    SafePipe,
    CurrencyFormatPipe,
    TranslateModule,
    ProductInformationComponent,
    PageTitleComponent,
    CategoryNavigationTrayComponent,
    CurrencyPipe,
    ParseNamePipe,
    ParsePsidPipe,
    NumberPipe,
    DecimalPipe,
    MediaProductComponent,
    MediaProductModalComponent,
    CustomFontScaleDirective,
    SwatchesColorComponent,
    AplImgSizePipe,
    SmartPriceComponent
  ],
  providers: [
    ParseNamePipe,
    ParsePsidPipe,
    OrderByPipe,
    CurrencyPipe,
    CurrencyFormatPipe,
    AplImgSizePipe,
    DecimalPipe
  ]
})
export class SharedModule { }
