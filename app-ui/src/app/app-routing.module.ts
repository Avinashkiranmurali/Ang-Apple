import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuard } from './auth/auth.guard';
import { TranslateResolver } from '@app/resolver/translate-resolver';
import { UserResolver } from '@app/resolver/user-resolver';
import { ProgramResolver } from '@app/resolver/program-resolver';
import { CategoryResolver } from '@app/resolver/category-resolver';
import { SessionResolver } from '@app/resolver/session-resolver';
import { MaintenanceComponent } from '@app/components/maintenance/maintenance.component';
import { ErrorComponent } from '@app/components/error/error.component';
import { LoginErrorComponent } from '@app/components/login-error/login-error.component';

const routes: Routes = [
  {
    path: 'store',
    canActivate: [AuthGuard], loadChildren: () => import('./modules/store/store.module').then(mod => mod.StoreModule),
    resolve : {
      message: TranslateResolver,
      user: UserResolver,
      program: ProgramResolver,
      mainNav: CategoryResolver,
      session: SessionResolver
    },
    data : {
      brcrumb: 'Store',
      brcrumbVal: '',
      theme: 'main-store',
      pageName: 'STORE'
    }
  },
  {
    path: 'maintenance',
    component: MaintenanceComponent
  },
  {
    path: 'error',
    component: ErrorComponent,
    data : {
      pageName: 'Error'
    }
  },
  {
    path: 'login-error',
    component: LoginErrorComponent,
    data : {
      pageName: 'Error'
    }
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
  onSameUrlNavigation: 'reload',
    scrollPositionRestoration: 'enabled',
    anchorScrolling: 'enabled',
    relativeLinkResolution: 'legacy'
})
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
