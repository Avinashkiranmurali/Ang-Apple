import { Directive, Input, ElementRef, HostListener, OnInit, Renderer2, Inject } from '@angular/core';
// import { number } from '@app/pipes/number.pipe';
import { DOCUMENT } from '@angular/common';

@Directive({
  selector: '[appConfigOption]'
})
export class ConfigOptionDirective implements  OnInit{

  constructor(private eleRef: ElementRef, private renderer: Renderer2) {
  }

  @Input() optset;
  @Input() optdata;
  @Input() config;
  @Input() messages;
  @Input() indexVal;
  @Input() fromPrice;

  selection: string;
  modelKey: string;
  modelVal: string;
  optvalue: string;
  optkey: string;

  width: number;
  swatchImage: string;
  isProductRed: boolean;
  isWatchModel: boolean;
  configOption: string;
1;
  ngOnInit(): void {
    const recaptchaContainer = this.renderer.createElement('div');
    this.renderer.appendChild(document.body, recaptchaContainer);
    this.getconfigOption();
   }
   @HostListener('window:resize', ['$event'])
  getScreenSize(event?) {
    this.width = window.innerWidth;
  }
  getconfigOption(){

    const optionData = this.optdata;
    const optionSet = this.optset;
    this.optvalue = optionData['value'];
    this.optkey = optionData['key'];
    this.swatchImage = optionData['image'];
    this.isProductRed = this.optkey.search('product_red') >= 0 || this.optvalue.search('(PRODUCT)') >= 0;
    this.isWatchModel = optionSet['name'] === 'caseSize';
    // Check for watch model
    // if (this.isWatchModel) {
    //   this.modelKey = this.optkey.replace(' ', '');
    //   this.modelVal = this.optvalue.replace(' ', '');
    //   el.addClass('watch-case');
    //   el.append('<img src="' + this.getWatchImg(this.modelKey) + '" alt="' + this.modelVal + '"/>');
    //   el.append('<ul>' +
    //     '<li aria-label="' + messages[this.configOption + '-' + this.modelVal + 'Height'] + ': ' + messages.caseHeight + '">' + messages.caseHeight + ': ' + messages[this.configOption + '-' + this.modelVal + 'Height'] + '</li>' +
    //     '<li aria-label="' + messages[this.configOption + '-' + this.modelVal + 'Width'] + ': ' + messages.caseWidth + '">' + messages.caseWidth + ': ' + messages[this.configOption + '-' + this.modelVal + 'Width'] + '</li>' +
    //     '<li aria-label="' + messages[this.configOption + '-' + this.modelVal + 'Depth'] + ': ' + messages.caseDepth + '">' + messages.caseDepth + ': ' + messages[this.configOption + '-' + this.modelVal + 'Depth'] + '</li>' +
    //     '</ul>');
    // }

    let optionElement = this.eleRef.nativeElement;
    // Check for 'swatchImageURL' in the title
    if (this.swatchImage) {
       this.eleRef.nativeElement.insertAdjacentHTML('beforeend', '<div class="swatch"><img' +
        ' class="swatch-img"' +
        ' src="' + this.swatchImage + '" alt="" width="32"' +
        ' height="32"></div>');
       optionElement = this.eleRef.nativeElement.getElementsByClassName('swatch')[0];
    }

    // Check to see if color value is (PRODUCT) RED
    if (this.isProductRed) {
      const imgPath = this.config['imageServerUrl'] + '/apple-gr/assets/img/product-red-logo.png';
      optionElement.insertAdjacentHTML('beforeend', '<span class="product-red"><img src="' + imgPath + '" ' +
        ' alt="' + this.optvalue + '"/></span>');
    } else {
      optionElement.insertAdjacentHTML('beforeend', '<span class="option-text">' + this.optvalue + '</span>');
    }
  }
  getWatchImg(size: string) {
    const urlString = this.config['imageServerUrl'] + '/apple-gr/assets/img/customizable/';
    const locale = 'en_US'; // TO-DO Customize for locale
    if (this.width < 720) {
      return urlString + size.trim() + 'FaceSize_' + this.localeCountry(locale) + '_mobile.png';
    } else {
      return  urlString + size.trim() + 'FaceSize_' + this.localeCountry(locale) + '.png';
    }

  }
  localeCountry(loc: string) {
    const  upperLoc = loc.toUpperCase();
    const  locSplit = upperLoc.split('_');
    if (locSplit[0] !== 'EN') {
      return locSplit[0];
    } else {
      return locSplit[1] === 'US' ? 'US' : 'UK';
    }
  }
}
