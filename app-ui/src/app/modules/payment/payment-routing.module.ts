import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { PaymentComponent } from './payment.component';
import { SelectComponent } from './select/select.component';
import { SplitComponent } from './split/split.component';
import { CcEntryComponent } from './cc-entry/cc-entry.component';
import { FinanceComponent } from './finance/finance.component';

const routes: Routes = [
  {
    path: '',
    component: PaymentComponent,
    children: [
      {
        path: '',
        component: SelectComponent,
        data: {
          theme: 'payment-options',
          pageName: 'PAY_OPTIONS',
          brcrumb: 'Store',
          brcrumbVal: ''
        }
      },
      {
        path: 'split',
        component: SplitComponent,
        data: {
          theme: 'payment-options',
          pageName: 'PAY_OPTIONS',
          brcrumb: 'Store',
          brcrumbVal: ''
        }
      },
      {
        path: 'card',
        component: CcEntryComponent,
        data: {
          theme: 'payment-options',
          pageName: 'PAY_OPTIONS',
          brcrumb: 'Store',
          brcrumbVal: ''
        }
      },
      {
        path: 'finance',
        component: FinanceComponent,
        data: {
          theme: 'payment-options',
          pageName: 'PAY_OPTIONS',
          brcrumb: 'Store',
          brcrumbVal: ''
        }
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class PaymentRoutingModule { }
