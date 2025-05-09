import { Directive, Input, ElementRef, OnInit } from '@angular/core';
import { SharedService } from '@app/modules/shared/shared.service';
import { max } from 'rxjs/operators';

@Directive({
  selector: '[appCustomFontScale]'
})
export class CustomFontScaleDirective implements OnInit {

  constructor(
    private sharedService: SharedService,
    private eleRef: ElementRef) { }

  @Input() maxFontSize: number;
  @Input() minFontSize: string;

  ngOnInit(): void {
    // Create observer
    const observer = new ResizeObserver(() => {
      this.customizeFontScale(this.eleRef.nativeElement);
    });

    // Add element (observe)
    observer.observe(this.eleRef.nativeElement);
  }

  customizeFontScale(ele) {
    if (window.innerWidth < 430) {
      const minFontSize = parseInt(this.minFontSize?.toString(), 10);
      const maxFontSize = this.maxFontSize;
      let elemStyle;
      let elemLineHeight: number;
      let noOfLines: number;
      let height: number;

      for (let f = maxFontSize; f >= minFontSize; f--) {
        ele.style.fontSize = this.sharedService.convertToRemUnit(f) + 'rem'; // apply font size
        elemStyle = window.getComputedStyle(ele);
        elemLineHeight = parseInt(elemStyle.getPropertyValue('line-height'), 10);
        height = parseInt(ele.getElementsByClassName('category-name-text')[0].offsetHeight, 10);
        noOfLines = parseInt((height / elemLineHeight)?.toString(), 10);
        if (noOfLines === 1) {
          break;
        }
      }
    }
    else {
      ele.style.fontSize = this.sharedService.convertToRemUnit(this.maxFontSize) + 'rem';
    }
  }

}
