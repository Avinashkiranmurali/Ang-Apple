import { BrowserModule } from '@angular/platform-browser';
import { NgModule, ErrorHandler } from '@angular/core';
import { HTTP_INTERCEPTORS, HttpClientModule, HttpClientJsonpModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AppRoutingModule } from '@app/app-routing.module';
import { LoaderComponent } from '@app/loader/loader.component';
import { LoaderService } from '@app/loader/loader.service';
import { LoaderInterceptorService } from '@app/loader/loader-interceptor.service';
import { JwtInterceptor } from '@app/auth/jwt.interceptor';
import { AppComponent } from '@app/app.component';
import { BaseService } from '@app/services/base.service';
import { XsrfService } from '@app/init/xsrf.service';
import { LayoutModule } from '@angular/cdk/layout';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { SharedModule } from './modules/shared/shared.module';
import { TranslateModule, TranslateLoader, MissingTranslationHandler } from '@ngx-translate/core';
import { CustomTranslateLoaderService } from './services/custom-translate-loader.service';
import { MissingTranslationHandlerService } from '@app/services/missing-translation-handler.service';
import { GlobalHandlerErrorService } from '@app/services/global-handler-error.service';
import { MaintenanceComponent } from '@app/components/maintenance/maintenance.component';
import { ErrorComponent } from './components/error/error.component';
import { LoginErrorComponent } from './components/login-error/login-error.component';
import { AcknowledgeTcModalComponent } from './components/modals/acknowledge-tc-modal/acknowledge-tc-modal.component';
import { PrintComponent } from './components/print-page/print.component';
import { SavePageComponent } from './components/save-page/save-page.component';

@NgModule({
  declarations: [
    AppComponent,
    LoaderComponent,
    MaintenanceComponent,
    ErrorComponent,
    LoginErrorComponent,
    AcknowledgeTcModalComponent,
    PrintComponent,
    SavePageComponent
  ],
  imports: [
    CommonModule,
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    HttpClientJsonpModule,
    ReactiveFormsModule,
    LayoutModule,
    BrowserAnimationsModule,
    NgbModule,
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useClass: CustomTranslateLoaderService,
      },
      missingTranslationHandler: {
        provide: MissingTranslationHandler,
        useClass: MissingTranslationHandlerService
      }
    }),
    SharedModule
  ],
  exports: [],
  providers: [
    XsrfService,
    BaseService,
    LoaderService,
    { provide: HTTP_INTERCEPTORS, useClass: LoaderInterceptorService, multi: true },
    { provide : HTTP_INTERCEPTORS, useClass : JwtInterceptor, multi : true },
    {
      provide: ErrorHandler,
      useClass: GlobalHandlerErrorService
    }
  ],
  bootstrap: [AppComponent]
})

export class AppModule { }
