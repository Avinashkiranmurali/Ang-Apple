import { Component, Inject, OnDestroy, OnInit, Renderer2 } from '@angular/core';
import { DOCUMENT, formatNumber, PlatformLocation } from '@angular/common';
import { animateChild, query, transition, trigger } from '@angular/animations';
import { NavStoreService } from '@app/state/nav-store.service';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { CategoryService } from '@app/services/category.service';
import { TemplateService } from '@app/services/template.service';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';
import { Config } from '@app/models/config';
import { Params } from '@app/models/params';
import { Category } from '@app/models/category';
import { Messages } from '@app/models/messages';
import { ActivatedRoute, ActivationEnd, NavigationEnd, Router, RouterEvent } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { TranslateService } from '@ngx-translate/core';
import { SessionService } from '@app/services/session.service';
import { TransitionService } from '@app/transition/transition.service';
import { Subscription } from 'rxjs';
import { AnalyticsService } from '@app/analytics/analytics.service';
import { Five9Service } from '@app/services/five9.service';
import { MatomoService } from '@app/analytics/matomo/matomo.service';
import { AppConstants } from '@app/constants/app.constants';
import { FooterDisclaimerService } from '@app/modules/footer/footer-disclaimer.service';
import { NotificationRibbonService } from '@app/services/notification-ribbon.service';
import { Idle } from '@ng-idle/core';
import { Keepalive } from '@ng-idle/keepalive';
import { IdleService } from '@app/services/idle.service';

@Component({
  selector: 'app-store',
  templateUrl: './store.component.html',
  styleUrls: ['./store.component.scss'],
  animations: [
    trigger('transition', [
      transition(':enter, :leave', [
        query('@fadeInOut', animateChild()),
      ]),
    ]),
  ]
})

export class StoreComponent implements OnDestroy, OnInit {
  messages: Messages;
  user: User;
  program: Program;
  config: Config;
  mainNav: Array<Category>;
  pageData: object;
  isEnableCustomNotificationRibbon: boolean;
  persistCustomNotificationRibbon: boolean;
  notificationRibbonEnabledForPages: Array<string>;
  isShowNotificationRibbon: boolean;
  isEnableNotificationRibbon: boolean;
  notificationRibbonMessage: string;
  pageTitle: string;
  params: Params;
  showOverlay: boolean;
  kountEnabled: boolean;
  private subscriptions: Subscription[] = [];
  searchedKey: string;
  pageName: string;
  enableIframeResizer: boolean;
  fragment: string;
  routeObj = {};
  previousRouterLink = '';
  constructor(
    public messageStore: MessagesStoreService,
    public userStore: UserStoreService,
    public mainNavStore: NavStoreService,
    private categoryService: CategoryService,
    private templateService: TemplateService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private titleService: Title,
    private translateService: TranslateService,
    private sessionService: SessionService,
    @Inject(DOCUMENT) private document: Document,
    private transitionService: TransitionService,
    private renderer2: Renderer2,
    private platFormlocation: PlatformLocation,
    private analyticsService: AnalyticsService,
    private five9Service: Five9Service,
    private matomoService: MatomoService,
    private notificationRibbonService: NotificationRibbonService,
    private footerDisclaimer: FooterDisclaimerService,
    private idle: Idle,
    private keepalive: Keepalive,
    private idleService: IdleService
  ) {
    this.router.events.subscribe((event: RouterEvent) => {
      switch (true) {
        case event instanceof NavigationEnd: {
          const nonShoppingState = ['/store/cart', '/store/checkout', '/store/configure/filter/engrave', '/store/engrave',
            '/store/gift-promo', '/store/confirmation', '/store/order-history', '/store/order-history/status',
            '/store/postback', '/store/terms', '/store/faqs', '/store/shipping-address', '/store/payment',
            '/store/payment/select', '/store/payment/card', '/store/payment/split', '/store/related-products/'];
          if (!(nonShoppingState.some(x => event.url.includes(x)))) {
            sessionStorage.setItem('previousUrl', JSON.stringify(event.url));
          }
          this.searchedKey = event.url.split('/')[(event['url'].split('/').length - 1)];
          const rt = this.getChild(this.activatedRoute);

          rt.params.subscribe(params => {
            this.params = {
              category: params.category ? params.category : '',
              subcat: params.subcat ? params.subcat : '',
              addCat: params.addCat ? params.addCat : '',
              psid: params.psid ? params.psid : '',
            };
          });

          this.activatedRoute.data.subscribe(data => {
            this.mainNav = data.mainNav;
          });

          rt.fragment.subscribe(fragmentValue => {
            this.fragment = fragmentValue;
          });

          rt.data.subscribe(data => {
            this.setTitle(data, event.url);
            this.pageName = data.pageName;
            const isPageAccessible = this.userStore.isPageAccessible(this.pageName);

            if (!isPageAccessible) {
              if (!this.config.fullCatalog) {
                setTimeout(() => {
                  this.router.navigate(['/store/browse/watch']);
                }, 100);
              }
              else if (this.pageName !== 'STORE') {
                setTimeout(() => {
                  this.router.navigate(['/store']);
                }, 100);
              }
            } else {
              const currentUrl = event.url.split('?');
              if (this.previousRouterLink !== currentUrl[0]) {
                this.matomoService.broadcast(AppConstants.analyticServices.MATOMO + AppConstants.analyticServices.EVENTS.ROUTE, {
                  payload: {
                    routeName: this.pageName,
                    location: location.href
                  }
                });
              }
              this.previousRouterLink = currentUrl[0];
            }
          });
          break;
        }
        case event instanceof ActivationEnd: {
          if (event['snapshot'].firstChild?.data.pageName && event['snapshot'].firstChild?.data.pageName !== 'STORE-PARENT') {
            this.enableCustomRibbon(event['snapshot'].firstChild?.data.pageName);
          }
          break;
        }
        default: {
          break;
        }
      }
    });
    this.messages = messageStore.messages;
    this.user = this.userStore.user;
    this.program = this.userStore.program;
    this.config = this.userStore.config;
    this.loadBannerStyles();
    this.notificationRibbonEnabledForPages = (this.program.config.notificationRibbonEnabledForPages !== undefined) ? this.program.config.notificationRibbonEnabledForPages.split(',') : [];
    this.isEnableNotificationRibbon = false;
    this.document.documentElement.lang = this.user.locale.replace('_', '-');
    this.setFavIcon();

    this.subscriptions.push(
      this.transitionService.showOverlay$.subscribe(data => {
        this.showOverlay = data;
      })
    );

    this.kountEnabled = this.config.kountEnabled;
    this.enableIframeResizer = this.config.enableIframeResizer;
    if (this.enableIframeResizer){
      const idleConfig = {
        idle: parseInt(this.config.idleSeconds, 10), // Value will be in seconds
        interval: parseInt(this.config.idleInterval, 10),
        timeout: parseInt(this.config.idleTimeout, 10)
     };
      idleService.init(this.idle, this.keepalive, idleConfig);
    }
  }

  setTitle(currentData, url){
     if (this.messages && currentData) {
       const productTitle = '';
       let subCategory;
       let catName;
       // let detailsName; TODO
       const stParams = {catname: '', subcatname: '', addcatname: '', detailsname: ''};

       let attrs = 'breadcrumb' + currentData.brcrumb;
       if (currentData.appendTitle) {
           attrs = attrs + '_' + (currentData.appendTitle);
       }

       const category = this.mainNav?.filter(e => e.slug === this.params.category);
       if (this.params?.psid && currentData.pageName === 'PDP') {

        setTimeout(() => {
          stParams.detailsname = this.userStore.detailsname;
          this.setPageTitle(attrs, productTitle, null, stParams);
        }, 2500);
      } else if (url.indexOf('webshop') > -1) {
         this.setPageTitle('webShopHeading_' + this.params.category, productTitle, null, stParams);
       }
       else {
         if (category && category.length) {
           stParams.catname = category[0].i18nName;
           if (this.params.subcat) {
             subCategory = category[0].subCategories.filter(e => e.slug === this.params.subcat);
           }
         }
         if (subCategory && subCategory.length) {
           stParams.subcatname = subCategory[0].i18nName;
           if (this.params.addCat) {
             catName = subCategory[0].subCategories.filter(e => e.slug === this.params.addCat);
           }
         }
         if (catName && catName.length) {
           stParams.addcatname = catName[0].i18nName;
           if (this.params.psid) {
             // TODO
             // configService.getProducts("/" + decodeURIComponent($stateParams.psid)).then(
             //     function (data) {
             //         stParams.detailsname = data.data.name;
             //         this.setPageTitle(attrs, productTitle);
             //     });
           }
         }

           if (!(this.params.psid && currentData.pageName === 'PDP')) {
             this.setPageTitle(attrs, productTitle, currentData, stParams);
            }else if (!catName) {
              this.setPageTitle('breadcrumbResults', productTitle, currentData, stParams);
            }
       }
     }
  }

  setPageTitle(attrs, productTitle, currentData, stParams) {
    const msgTest = this.messages.hasOwnProperty(attrs);
    const msg = (msgTest) ? this.messages[attrs] : '';

    if (msg !== '') {
      const paramsType = {
        ...((stParams.detailsname ) && { detailsname : stParams.detailsname }),
        ...((stParams.addcatname ) && { addcatname : stParams.addcatname }),
        ...((stParams.subcatname ) && { subcatname : stParams.subcatname }),
        ...((stParams.catname ) && { catname : stParams.catname })
      };
      productTitle =  this.translateService.instant(attrs, {
        ...((paramsType) && {stParams: paramsType }),
        ...((!!paramsType) && { breadcrumbObjs: {params : {keyword : this.searchedKey, orderId: this.searchedKey}}})
      });
    }

    let title = '';
    if (currentData?.configuredTitle) {
      title = productTitle || '';
    } else if (decodeURIComponent (productTitle + (this.messages.programDisplayName || '')) ) {
      title = decodeURIComponent(productTitle + ' - ' + this.messages.programDisplayName);
    }

    this.pageTitle = title || this.messages.defaultPageTitle || '';

    // Below line will trigger the page title
    this.titleService.setTitle(this.pageTitle);
    const location = (this.platFormlocation as any).location.href || document.URL;
    if ('parentIFrame' in window) {
      if (this.routeObj['location'] !== location) {
        this.routeObj['location'] = location;
        const ev = new CustomEvent('route', {
          detail: {
            title: this.pageTitle,
            location: (this.platFormlocation as any).location.href || document.URL
          }
        });
        window.dispatchEvent(ev);
      }
    }
 }

  getChild(activatedRoute: ActivatedRoute) {
    if (activatedRoute.firstChild) {
      return this.getChild(activatedRoute.firstChild);
    } else {
      return activatedRoute;
    }
  }

  ngOnInit(): void {
    this.persistCustomNotificationRibbon = sessionStorage.getItem('persistCustomNotificationRibbon') ? (sessionStorage.getItem('persistCustomNotificationRibbon') === 'true') : false;
    this.mainNav = this.mainNavStore.mainNav;
    /* eslint-disable-next-line , , , , , ,  */

    /* eslint-disable-next-line  */

    this.router.events.subscribe(
      (event: RouterEvent) => {
        if (event instanceof NavigationEnd) {
          this.sessionService.getSession();
        }
      });

    if (this.enableIframeResizer) {
      const script = this.renderer2.createElement('script');
      script.type = 'text/javascript';
      script.src = 'assets/iframeResizer/iframeSizer.contentWindow.js';
      this.renderer2.appendChild(this.document.body, script);
    }

    const data = 'enableIframeResizer = ' + this.enableIframeResizer + '; \n if( self == top  || enableIframeResizer) {\n' +
      '      document.documentElement.style.display = \'block\' ;\n' +
      '      document.documentElement.style.visibility = \'visible\' ;\n' +
      '    } else {\n' +
      '      top.location = self.location ;\n' +
      '    }';
    const body = this.document.getElementsByTagName('head')[0];
    const initialScript = this.document.createElement('script');
    initialScript.type = 'text/javascript';
    initialScript.text = data;
    body.appendChild(initialScript);

    setTimeout(() => {
      if (this.enableIframeResizer){
        const ev = new CustomEvent('setPointsBalance', {
          detail: {
            formatted: formatNumber(this.user.balance, this.user.locale),
            integer: this.user.balance
          }
        });
        window.dispatchEvent(ev);

        const ssoEvent = new CustomEvent('setSSO', {
          detail: {
            persistentId: this.config.loginRequired ? null : this.user.userid
          }
        });
        window.dispatchEvent(ssoEvent);
      }
    }, 3500);

  }

  loadBannerStyles(): void {
    const dynamicStyles = new Map([
      ['icomoon', 'apple-gr/assets/css/icomoon.css'],
      ['family-banner', 'apple-gr/assets/css/banners/family-banners.css'],
      ['family-banner-mobile', 'apple-gr/assets/css/banners/family-banners-mobile.css'],
      ['product-banner', 'apple-gr/assets/css/banners/product-banner.css'],
      ['global-overrides', 'apple-gr/assets/css/global-overrides.css'],
      ['sub-nav', 'apple-gr/assets/css/sub-nav.css'],
      ['overrides', 'apple-gr/vars/' + this.user.varId.toLowerCase() + '/css/override.css']
    ]
    );

    if (this.config.SFProWebFont) {
      dynamicStyles.set('sf-pro-font', 'apple-gr/assets/css/sf-pro-fonts.css');
    }

    const head = this.document.getElementsByTagName('head')[0];
    const title = this.document.getElementsByTagName('title')[0];

    for (const [name, url] of dynamicStyles) {

      const styleFullPath = this.config['imageServerUrl'] + '/' + url + '?' + this.program.sessionConfig.imageServerBuildNumber;
      const style = this.document.createElement('link');

      style.id = name;
      style.rel = 'stylesheet';
      style.href = styleFullPath;
      if (name.indexOf('overrides') >= 0 ) {
        head.appendChild(style);
      } else {
        head.insertBefore(style, title);
      }
    }
  }

  setFavIcon(): void {
    this.document.getElementById('favIcon').setAttribute('href', this.config.imageServerUrl + this.messages.favIconUrl);
  }

  onActivate(e) {
    this.pageData = (e.activatedRoute) ? e.activatedRoute.data.getValue() : {};
  }

  skipMainContent(): void {
    const scrollToObj = document.getElementById('mainContent');
    if (scrollToObj.id === 'mainContent') {
      const focusable = scrollToObj.querySelectorAll('*:not([style*=display]), :not([style*=none]), h2, h1, h3, button, [href]:not(section), input, select, textarea, [tabindex]:not([tabindex="-1"])');
      const firstFocusable = focusable[0] as HTMLElement;
      firstFocusable.setAttribute('tabIndex', '-1');
      setTimeout(() => {
        firstFocusable.focus();
        document.getElementById('mainContent').scrollIntoView({ behavior: 'smooth' });
      }, 200);
    }
  }

  // Custom Ribbon
  enableCustomRibbon(pageName): void {
    if (this.notificationRibbonEnabledForPages && this.notificationRibbonEnabledForPages.includes(pageName) && !this.notificationRibbonService.getCustomRibbonClosed()) {
      this.isEnableCustomNotificationRibbon = true;
      this.notificationRibbonService.setCustomRibbonShow(true);
    } else {
      this.isEnableCustomNotificationRibbon = false;
      this.notificationRibbonService.setCustomRibbonShow(false);
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }
}
