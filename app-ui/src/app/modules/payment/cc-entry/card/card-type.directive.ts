import { Directive, Renderer2, ElementRef, HostListener } from '@angular/core';

@Directive({
  selector: '[appCardType]'
})
export class CardTypeDirective{
  constructor(private elem: ElementRef, private render: Renderer2) {  }
  @HostListener('input')
  onChange() {
    const value =
      (/^5[1-5][0-9]/.test(this.elem.nativeElement.value)) ? 'MASTERCARD'
        : (/^4/.test(this.elem.nativeElement.value)) ? 'VISA'
        : (/^3[47][0-9]/.test(this.elem.nativeElement.value)) ? 'AMEX'
          : (/^6(?:011|5[0-9])[0-9]/.test(this.elem.nativeElement.value)) ? 'DISCOVER'
            : (/^(4026|417500|4405|4508|4844|4913|4917)\d+$/.test(this.elem.nativeElement.value)) ? 'ELECTRON'
              : (/^(5018|5020|5038|5612|5893|6304|6759|6761|6762|6763|0604|6390)\d+$/.test(this.elem.nativeElement.value)) ? 'MAESTRO'
                : (/^(636)\d+$/.test(this.elem.nativeElement.value)) ? 'DANKORT'
                  : (/^(5019)\d+$/.test(this.elem.nativeElement.value)) ? 'INTERPAYMENT'
                    : (/^(62|88)\d+$/.test(this.elem.nativeElement.value)) ? 'UNIONPAY'
                      : (/^3(?:0[0-5]|[68][0-9])[0-9]{11}$/.test(this.elem.nativeElement.value)) ? 'DISCOVER'
                        : (/^(?:2131|1800|35\d{3})\d{11}$/.test(this.elem.nativeElement.value)) ? 'JCB'
                          : null;
    this.elem.nativeElement.cardType = value;
    return value;
  }

}
