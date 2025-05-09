import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { BaseService } from '@app/services/base.service';
import { UserStoreService } from '@app/state/user-store.service';

@Injectable({
  providedIn: 'root'
})
export class TealiumService extends BaseService {

  tealiumEndPoint: string;

  constructor(
    private userStoreService: UserStoreService,
    @Inject(DOCUMENT) private document: Document
  ) {
    super();
  }

  insertAfter(newNode: HTMLScriptElement, referenceNode: Node) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
  }

  loadInitialScript() {
    this.tealiumEndPoint = this.userStoreService.config.tealiumEndPoint;
    const body = this.document.getElementsByTagName('body')[0];
    const initialScript = this.document.createElement('script');
    initialScript.type = 'text/javascript';
    initialScript.text = 'var utag_data = {}';
    body.insertBefore(initialScript, body.firstChild);
    this.loadTealiumScript();
  }

  loadTealiumScript() {
    const body = this.document.getElementsByTagName('body')[0];
    const childScript = body.getElementsByTagName('script')[0];
    const tealiumScript = this.document.createElement('script');
    tealiumScript.type = 'text/javascript';
    tealiumScript.text = `(function(a,b,c,d) {
                            a='${this.tealiumEndPoint}';
                            b=document;c='script';d=b.createElement(c);d.src=a;d.type='text/java'+c;d.async=true;
                            a=b.getElementsByTagName(c)[0];a.parentNode.insertBefore(d,a);
                          })();`;
    this.insertAfter(tealiumScript, childScript);
  }

}
