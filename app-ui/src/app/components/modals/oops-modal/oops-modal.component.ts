import { Component, OnInit, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TemplateStoreService } from '@app/state/template-store.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-oops-modal',
  templateUrl: './oops-modal.component.html',
  styleUrls: ['./oops-modal.component.scss']
})
export class OopsModalComponent implements OnInit {

  @Input() template: string;
  @Input() pData: object;

  buttonColor: string | null = null;

  constructor(private activeModal: NgbActiveModal, private templateStoreService: TemplateStoreService, private router: Router ) {
    this.buttonColor = this.templateStoreService.buttonColor ? this.templateStoreService.buttonColor : '';
  }

  ngOnInit(): void { }

  complete(targetLink): void {
    this.activeModal.close();
    this.router.navigate([targetLink]);
  }

  cancel(): void {
    this.activeModal.close();
  }

}
