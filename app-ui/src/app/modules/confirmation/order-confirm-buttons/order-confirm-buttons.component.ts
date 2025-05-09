import { Component, OnInit, Input } from '@angular/core';
import { Router } from '@angular/router';
import { TemplateService } from '@app/services/template.service';
import { UserStoreService } from '@app/state/user-store.service';
import { Config } from '@app/models/config';
import { AppConstants } from '@app/constants/app.constants';
import { SharedService } from '@app/modules/shared/shared.service';

@Component({
  selector: 'app-order-confirm-buttons',
  templateUrl: './order-confirm-buttons.component.html',
  styleUrls: ['./order-confirm-buttons.component.scss']
})
export class OrderConfirmButtonsComponent implements OnInit {
  @Input() displayContinueShopping: boolean;
  paymentTemplate: string;
  config: Config;
  buttonColor: string;

  constructor(
    public templateService: TemplateService,
    private router: Router,
    private userStore: UserStoreService,
    public sharedService: SharedService
  ) {
    this.config = this.userStore.config;
    this.paymentTemplate = this.config.paymentTemplate;
    this.buttonColor = this.templateService.getBtnColor();
  }

  ngOnInit(): void {
  }

  continueShopping() {
    if (this.paymentTemplate === AppConstants.paymentTemplate.cash_subsidy) {
      const storedUrls = sessionStorage.getItem('sessionURLs');
      const varUrls = JSON.parse(storedUrls);
      sessionStorage.clear();
      window.location.href = varUrls.signOutUrl;
    } else {
      this.router.navigate(['/store']);
    }
  }

}
