import { DOCUMENT } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Inject, Injectable, RendererFactory2 } from '@angular/core';
import { ModalsService } from '@app/components/modals/modals.service';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { BaseService } from './base.service';
import { Renderer2 } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class TimedoutModalService extends BaseService {

  private renderer: Renderer2;

  constructor(
    private http: HttpClient,
    private modalService: ModalsService,
    public rendererFactory: RendererFactory2,
    @Inject(DOCUMENT) private document: HTMLDocument) {
    super();
    this.renderer = rendererFactory.createRenderer(null, null);
  }

  checkRemainingTime(): Observable<object> {
    const url = this.baseUrl + 'timedOut?nocache=' + new Date().getTime();
    return this.http.get(url, this.httpOptions).pipe(
      map((response) => response),
      catchError((error: HttpErrorResponse) => {
          // general and service level error actions
          this.handleError(error);
          return throwError(error);
      })
    );
  }

  showTimedoutModal(data) {
    if (data && data['timedOut']) {
      const iframe = this.document.createElement('iframe');
      const parentIframeDiv = this.document.createElement('div');
      if (this.modalService.hasAnyOpenModal()) {
        this.modalService.dismissAllModals();
      }

      // find height to top ratio
      window.scrollTo(0, 0);
      const yaxis = (window.innerHeight / 2) - (383 / 2);
      const scrollBool = (window.innerWidth < 720).toString();

      // create iFrame's attributes
      iframe.setAttribute('src', data['timedOutUrl']);
      iframe.setAttribute('scrolling', scrollBool);
      iframe.setAttribute('id', 'MPConnectLogInFrame');
      iframe.setAttribute('sandbox', 'allow-same-origin');

      // create css, class and insert HTML for parent div container
      parentIframeDiv.style.top = yaxis + 'px';
      this.renderer.addClass(parentIframeDiv, 'parentIframeContainer');
      parentIframeDiv.append(iframe);

      // append the generated parent div container and overlay to 'body' tag
      this.document.body.append(parentIframeDiv);
      this.document.body.append('<div class="iframeOverlay"><br/></div>');
    } else {
      this.document.getElementsByClassName('.parentIframeContainer, .iframeOverlay');
    }
  }
}
