import { Component, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-idology-model',
  templateUrl: './idology-model.component.html'
})
export class IdologyModelComponent {

  @Input() validator;

  constructor(
    private activeModal: NgbActiveModal,
  ) { }

placeOrder(status: string) {
  this.activeModal.close(status);
}

}
