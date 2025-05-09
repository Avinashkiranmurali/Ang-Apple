import {
  Component,
  Input,
  Output,
  OnInit,
  EventEmitter
} from '@angular/core';
import { PaymentStoreService } from '@app/state/payment-store.service';
import { DecimalPipe } from '@angular/common';
import { CurrencyPipe } from '@app/pipes/currency.pipe';
import { Config } from '@app/models/config';
import { UserStoreService } from '@app/state/user-store.service';
import { ControlContainer } from '@angular/forms';

@Component({
  selector: 'app-card',
  templateUrl: './card.component.html',
  styleUrls: ['./card.component.scss']
})
export class CardComponent implements OnInit {
  state;
  config: Config;
  cardForm;
  translateParams: { [key: string]: string };
  creditCardTypes;
  cardInfo = {cardNumber: '', type: '', expiration: ''};
  trimmedCardNum;
  @Input() postCardDetailsError: Array<{[key: string]: string}>;
  @Output() newItemEvent = new EventEmitter<string>();
  constructor(
    public userStore: UserStoreService,
    public stateService: PaymentStoreService,
    private decimalPipe: DecimalPipe,
    private currencyPipe: CurrencyPipe,
    public controlContainer: ControlContainer
  ) {
    this.stateService.get().subscribe(data => {
      this.state =  data;
      const params = { ...this.translateParams };
      this.config = this.userStore.config;

      if (this.state.cart) {
        if (this.state.cart.cartTotal) {
          params.points = this.decimalPipe.transform(this.state.cart.cartTotal.price.points);
          if (this.config.showDecimal) {
            params.amount = this.currencyPipe.transform(this.state.cart.cartTotal.price.amount);
          } else {
            params.amount = this.currencyPipe.transform(this.state.cart.cartTotal.price.amount, '', 'symbol', '1.0-0');
          }
        }
      }
      if (this.state.selections && this.state.selections.payment && this.state.selections.payment.splitPayOption) {
        if (this.config.showDecimal) {
          params.cashToUse = this.currencyPipe.transform(this.state.selections.payment.splitPayOption.cashToUse);
        } else {
          params.cashToUse = this.currencyPipe.transform(this.state.selections.payment.splitPayOption.cashToUse, '', 'symbol', '1.0-0');
        }
      }
      this.translateParams = params;
    });
  }


  ngOnInit(): void {
    if (this.config.supportedCreditCardTypes !== 'NONE'){
      this.creditCardTypes = this.config.supportedCreditCardTypes.split(',');
    } else {
      this.creditCardTypes = null;
    }
    this.cardForm = this.controlContainer.control;
  }

  get cardNumberControl() {
    return this.cardForm.get('cardNumber');
  }

  get expirationControl() {
    return this.cardForm.get('expiration');
  }

  get securityCodeControl() {
    return this.cardForm.get('securityCode');
  }

  getCardType(val) {
    this.cardInfo.type = val.target.cardType;
    this.newItemEvent.emit(this.cardInfo.type);
  }

  formatDate(event) {
    const cursorPosition = event.target.selectionStart;
    let value = this.expirationControl.value;

    if (value === '' || (event.key === '/' && value.indexOf('/') === 2) || event.key === 'tab') {
      // allow "/" in the third character
      return;
    }

    value = value.replace(/\D/g, '');
    const expMonth = value.slice(0, 2);
    let expYear = value.slice(2, 4);
    expYear = (expYear !== '') ? '/' + expYear : '';
    const expiration = expMonth + expYear;

    if (expiration.length === 5) {
      this.cardInfo.expiration = expiration;
    }
    this.expirationControl.patchValue(expiration);
    // To update cursor position to recently added character in textBox
    if (event.keyCode === 8 || event.keyCode === 46 ) {
      event.target.setSelectionRange(cursorPosition, cursorPosition);
    }
  }

  formatExpDate(event) {
    setTimeout (() => {
      this.formatDate(event);
    }, 10);
  }

  bindCreditCardFocus() {
    const { cardNumber } = this.cardForm.controls;
    cardNumber.setValue(this.trimmedCardNum);
  }

  bindCreditCardBlur() {
    setTimeout (() => {
      this.formatCreditCard();
    }, 10);
  }

  formatCreditCard() {
    const input = this.cardForm.get('cardNumber');
    const { selectionStart } = input;
    const { cardNumber } = this.cardForm.controls;

    let trimmedCardNum = cardNumber.value.replace(/\s+/g, '');
    this.trimmedCardNum = trimmedCardNum;
    if (trimmedCardNum.length > 16) {
      trimmedCardNum = trimmedCardNum.substr(0, 16);
    }

    /* Convert the American Express to ####-######-##### format */
    const partitions = trimmedCardNum.startsWith('34') || trimmedCardNum.startsWith('37') ? [4, 6, 5] : [4, 4, 4, 4];

    const numbers = [];
    let position = 0;
    partitions.forEach(partition => {
      const part = trimmedCardNum.substr(position, partition);
      if (part) {
        numbers.push(part);
      }
      position += partition;
    });
    const a = cardNumber.errors;
    cardNumber.setValue(numbers.join('-'));
    const b = cardNumber.errors;
    if (!a && (a !== b)) {
      cardNumber.errors = null;
    }
    /* Keep caret position when user edits the field again */
    if (selectionStart < cardNumber.value.length - 1) {
      input.setSelectionRange(selectionStart, selectionStart, 'none');
    }
  }
}
