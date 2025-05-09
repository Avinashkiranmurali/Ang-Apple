import { AfterViewInit, Component, HostListener, Input, OnDestroy, OnInit, Injector, Renderer2, ViewChild } from '@angular/core';
import { TemplateStoreService } from '@app/state/template-store.service';
import { Router } from '@angular/router';
import { SharedService } from '@app/modules/shared/shared.service';
import { TranslateService } from '@ngx-translate/core';
import { Config } from '@app/models/config';
import { Subscription } from 'rxjs';
import { User } from '@app/models/user';
import { ModalsService } from '@app/components/modals/modals.service';
import { Messages } from '@app/models/messages';
import { NgbPopover } from '@ng-bootstrap/ng-bootstrap';
import { BreakPoint } from '@app/components/utils/break-point';
import { Five9SocialWidget } from '@app/services/five9.service';

@Component({
  selector: 'app-user-dropdown',
  templateUrl: './user-dropdown.component.html',
  styleUrls: ['./user-dropdown.component.scss']
})
export class UserDropdownComponent extends BreakPoint implements AfterViewInit, OnInit, OnDestroy {

  @Input() cartItemsTotalCount: number;
  @Input() messages: Messages;
  @Input() config: Config;
  @Input() user: User;
  @Input() popupClose: NgbPopover;
  userDropdownLinks: Array<string>;
  authenticatedBagItemList: string;
  public five9SocialWidget: Five9SocialWidget;
  @ViewChild('termsLink') termsLink;
  private subscriptions: Subscription[] = [];

  constructor(
    private router: Router,
    private sharedService: SharedService,
    private templateStoreService: TemplateStoreService,
    private translateService: TranslateService,
    public modalsService: ModalsService,
    public injector: Injector,
    private renderer: Renderer2
  ) {
    super(injector);
  }

  ngOnInit(): void {
    this.five9SocialWidget = window.Five9SocialWidget;
    this.subscriptions.push(
      this.translateService.get('authenticatedBagItemList', {glbImgDomain: this.config.imageServerUrl}).subscribe((resulst: string) => {
      this.authenticatedBagItemList = resulst;
    }));
    this.userDropdownLinks = (this.config['loginRequired']) ? this.templateStoreService.userDropdownLinks['unAuthenticated'] : this.templateStoreService.userDropdownLinks['authenticated'];
  }

  ngAfterViewInit() {
    if (this.termsLink) {
      const anchors = this.termsLink.nativeElement.getElementsByTagName('a') as HTMLAnchorElement[];

      for (const anchor of anchors) {
        if (!anchor.hasAttribute('data-external')) {
          this.renderer.listen(anchor, 'click', event => {
            const target = event.target as HTMLAnchorElement;
            const hash = anchor.hash.replace('#', '');
            const fragment = hash ? {fragment: hash} : undefined;
            const pathname = target.pathname;

            event.preventDefault();
            this.router.navigate([pathname], fragment);
          });
        }
      }
    }
  }

  @HostListener('click', ['$event'])
  onClick(event: MouseEvent) {
    // NgPopupClose event
    const targetElement = event.target as HTMLElement;
    if (targetElement instanceof HTMLAnchorElement === true || targetElement.parentElement instanceof HTMLAnchorElement){
      this.popupClose.close();
    }
    // If we don't have an anchor tag, we don't need to do anything.
    if (event.target instanceof HTMLAnchorElement === false || event.target['attributes']['data-external']) {
      return;
    }

    /*const target = event.target as HTMLAnchorElement;
    // Navigate to the path in the link
    if (target.hash.indexOf('#') > -1) {
      // Prevent page from reloading
      event.preventDefault();
      this.router.navigate([target.pathname], {fragment: target.hash.substr(1)});
    }*/
  }

  sessionTypeAction(action): boolean {
    this.sharedService.sessionTypeAction(action);
    return false;
  }

  openFive9Chat(): boolean {
    this.five9SocialWidget.maximizeChat(this.five9SocialWidget.options);
    return false;
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  openModal(): boolean {
    this.modalsService.openBrowseOnlyComponent();
    return false;
  }

}
