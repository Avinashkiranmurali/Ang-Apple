import { Component, OnDestroy, OnInit, ElementRef, ViewChild, Injector, HostListener } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { BreakPoint } from '@app/components/utils/break-point';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { UserStoreService } from '@app/state/user-store.service';
import { NavStoreService } from '@app/state/nav-store.service';
import { User } from '@app/models/user';
import { Config } from '@app/models/config';
import { NgbPopover } from '@ng-bootstrap/ng-bootstrap/popover/popover';
import { QuickLinksService } from '@app/services/quick-links.service';
import { QuickLink } from '@app/models/quick-link';
import { ModalsService } from '@app/components/modals/modals.service';
import { TemplateStoreService } from '@app/state/template-store.service';
import { CartItem } from '@app/models/cart';
import { Category } from '@app/models/category';
import { Messages } from '@app/models/messages';
import { Subscription } from 'rxjs';
import { AppConstants } from '@app/constants/app.constants';
import { CartService } from '@app/services/cart.service';
import { SharedService } from '@app/modules/shared/shared.service';
import { SessionService } from '@app/services/session.service';
import template from 'lodash/template';
import templateSettings from 'lodash/templateSettings';
import { PricingService } from '@app/modules/pricing/pricing.service';
import { Price } from '@app/models/price';
import { SmartPrice } from '@app/models/smart-price';

@Component({
  selector: 'app-nav',
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.scss']
})

export class NavComponent extends BreakPoint implements OnInit, OnDestroy {

  mainNav: Array<Category>;
  navTemplate: object;
  mobileNav: ElementRef;
  messages: Messages;
  user: User;
  config: Config;
  subNav: Array<any> = [];
  navLabel = 'Open Navigation';
  cartItems: Array<CartItem>;
  emptyCart: boolean;
  cartTooltipLoadError: boolean;
  loadingItems: boolean;
  cartItemsTotalCount: number;
  shoppingCartItemsMessage: string;
  isOpen = false;
  cartUpdateMsg: string = '';
  persistCartAbandonNotification = false;
  isCartAbandonNotificationEnabled = false;
  showSubNavBar: boolean;
  showSearchBox: boolean;
  displayCurtain = false;
  currentSlug: string;
  quickLinks: QuickLink[];
  showQuickLinks: boolean;
  pricingOption: string;
  isUnbundled: boolean;
  cartDiscountedSubtotal: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  cartSubtotal: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  cartTotals: Price = {
    amount: 0,
    points: 0,
    currencyCode: ''
  };
  smartPrice: SmartPrice;
  private subscriptions: Subscription[] = [];

  @ViewChild('search') search: ElementRef;
  @ViewChild('cartIcon') cartIcon: ElementRef;
  @ViewChild('bagMenu') bagMenu: NgbPopover;
  @ViewChild('mobileNav') set mobileNavData(element: ElementRef | null) {
    if (element){
      this.mobileNav = element;
    }
  }

  constructor(
    private messageStore: MessagesStoreService,
    private userStore: UserStoreService,
    private mainNavStore: NavStoreService,
    public cartService: CartService,
    public sharedService: SharedService,
    private router: Router,
    private route: ActivatedRoute,
    private quickLinkService: QuickLinksService,
    public injector: Injector,
    private modalsService: ModalsService,
    private templateStoreService: TemplateStoreService,
    public sessionService: SessionService,
    private pricingService: PricingService
  ) {
    super(injector);
    this.messages = this.messageStore.messages;
    this.user = this.userStore.user;
    this.config = this.userStore.config;
    this.navTemplate = this.templateStoreService.navigationTemplate;
    this.mainNav = this.mainNavStore.mainNav;
    this.showSubNavBar = this.config.showSubNavBar;
    this.isCartAbandonNotificationEnabled = this.config.loginRequired ? false : Boolean(this.config.isCartAbandonNotificationEnabled);
    // Get the router params and set the current active slug and set showSubNavBar
    this.router.events.subscribe(val => {
      if (val instanceof NavigationEnd) {
        let r = this.route;
        while (r.firstChild) {
          r = r.firstChild;
        }
        r.params.subscribe(params => {
          this.currentSlug = params.category;
        });
      }

      this.showSubNavBar = !(this.currentSlug === 'accessories' || !this.currentSlug);
      if (this.showSubNavBar) {
        const category: Category[] = this.mainNav.filter(nav => nav.slug === this.currentSlug);
        this.subNav = (category.length) ? category[0].subCategories : [];
      }
    });
    this.getQuickLinks();
    this.sharedService.isAbandonCartPopoverOpen().subscribe(isOpen => {
      if (isOpen) {
        this.closeBagMenu(this.bagMenu);
      }
    });
    this.subscriptions.push(
      this.cartService.getUpdateCartObj().subscribe(data => {
        this.updateCartData(data);
    }));
  }

  @HostListener('window:resize')
  onResize(): void {
    if (this.isOpen) {
      this.setPositionForBagPopup();
    }
  }

  openModal(): boolean {
    this.modalsService.openAnonModalComponent();
    return false;
  }

  ngOnInit(): void {
    this.persistCartAbandonNotification = sessionStorage.getItem('persistCartAbandonNotification') ? (sessionStorage.getItem('persistCartAbandonNotification') === 'true') : false;
    if(!this.persistCartAbandonNotification) {
      sessionStorage.setItem('persistCartAbandonNotification', 'true');
    }
    this.mainNav = this.mainNavStore.mainNav;
    this.initialSubnav(this.mainNav);
    this.showSearchBox = false;
    const urlSegments = this.router.url.split('/');

    this.currentSlug = (urlSegments && urlSegments.length >= 4 ) ? urlSegments[3] : '';

    if ( this.showSubNavBar && urlSegments.length >= 4 && urlSegments[3] !== AppConstants.ACCESSORIES){
        const category: Category[] = this.mainNav.filter(nav => nav.slug === urlSegments[3]);
        this.subNav = (category.length) ? category[0].subCategories : [];
    }

    this.loadingItems = false;
    this.getCart();
    this.subscribeCartItemData();
    this.subscriptions.push(
      this.cartService.cartUpdateMessage$.subscribe(data => {
        this.cartUpdateMsg = data || '';
      })
    );
  }

  changeSubnav(i): void {
    this.subNav = this.mainNav[i].subCategories;
    setTimeout(() => {
      document.getElementById('subnav-'+this.mainNav[i].subCategories[0].slug)?.focus();
    }, 100);
  }

  // Sets the initial Subnav on load based on the current active slug from the route
  initialSubnav(mainNav): void {
    // eslint-disable-next-line @typescript-eslint/prefer-for-of
    for ( let i = 0; i < mainNav.length; i++ ) {
      if ( mainNav[i].slug === this.currentSlug ) {
        this.subNav = mainNav[i].subCategories;

        break;
      }
    }
  }

  getTemplateURL(rel) {
    return (rel) ? '/apple-gr/service/template?url=/' + rel : '';
    // return (rel) ? $sce.trustAsResourceUrl($scope.glbImgDomain + '/' + rel) : '';
  }

  mobileNavToggle(mobileNav: NgbPopover): void {
    const bodyElement = document.getElementsByTagName('body')[0];
    const navElement = document.getElementsByClassName('new-nav-mobile')[0];
    if (!mobileNav.isOpen()) {
      bodyElement.classList.add('mobile-nav-open');
      navElement.classList.add('enabled');
      setTimeout(() => {
        mobileNav.open();
        this.navLabel = 'Close Navigation';
      }, 200);
    } else {
      bodyElement.classList.remove('mobile-nav-open');
      navElement.classList.remove('enabled');
      mobileNav.close();
      this.navLabel = 'Open Navigation';
    }
  }

  mobileNavClose(): void {
    const bodyElement = document.getElementsByTagName('body')[0];
    const navElement = document.getElementsByClassName('new-nav-mobile')[0];
    bodyElement.classList.remove('mobile-nav-open');
    navElement.classList.remove('enabled');
  }

  mobileNavOpen(): void {
    setTimeout(() => {
      Array.from(document.querySelectorAll('.main.nav-animation'))
        .forEach((item: HTMLElement) => {
          if (item.children[0].classList.contains('active')) {
            item.style.color = this.config.navBarColors['activeTextColor'];
          }
        });
    });
  }

  getQuickLinks() {
    this.subscriptions.push(
      this.quickLinkService.getQuickLinks().subscribe(res => {
      this.quickLinks = res;
    }));
  }

  showHideSearchBar(): void {
    this.showSearchBox = !this.showSearchBox;
    this.togglePageCurtain();
  }

  togglePageCurtain() {
    this.displayCurtain = !this.displayCurtain;
  }

  toggleQuickLinks(event: boolean){
    this.showQuickLinks = event;
  }

  focusSearchIcon(): void {
    this.search.nativeElement.focus();
  }

  popupTabEvent() {
    this.isOpen = true;
  }

  getCart(): void {
    this.subscriptions.push(
      this.cartService.getCart().subscribe(data => {
        if (!this.persistCartAbandonNotification && (data['cartItems'].length > 0) && this.isCartAbandonNotificationEnabled) {
          this.bagMenu.toggle();
          this.setPositionForBagPopup();
          this.persistCartAbandonNotification = true;
        } else {
          this.persistCartAbandonNotification = false;
        }
        this.updateCartData(data);
      }, error => {})
    );
  }

  updateCartData(cartData) {
    ({ option: this.pricingOption, isUnbundled: this.isUnbundled } = this.pricingService.getPricingOption());
    if (cartData.cartItems.length !== 0) {
      const pricingData = this.sharedService.getPriceData(cartData.cartTotal);
      this.cartSubtotal = (this.isUnbundled === true) ? (this.config.displayDiscountedItemPriceInPriceBreakdown && cartData.cartTotal.discountedItemsSubtotalPrice) ? {
        amount: cartData.cartTotal.discountedItemsSubtotalPrice.amount,
        points: cartData.cartTotal.discountedItemsSubtotalPrice.points
      } : {
        amount: cartData.cartTotal.itemsSubtotalPrice.amount,
        points: cartData.cartTotal.itemsSubtotalPrice.points
      } : {
        amount: cartData.cartTotal.price.amount,
        points: cartData.cartTotal.price.points
      };
      this.cartDiscountedSubtotal = pricingData.cartDiscountedSubtotal;
      this.smartPrice = cartData.smartPrice;
      this.cartTotals = pricingData.cartTotals;
      if (this.config.paymentTemplate === AppConstants.paymentTemplate.cash_subsidy) {
        this.cartSubtotal = cartData.displayCartTotal.itemsSubtotalPrice;
        this.cartTotals = cartData.displayCartTotal.price;
      }
    }
  }

  subscribeCartItemData(): void {
    this.subscriptions.push(
      this.cartService.cartItems$.subscribe(
        data => {
          this.cartItems = data['cartItems'];
          this.emptyCart = (this.cartItems.length === 0);
          this.cartItemsTotalCount = this.cartService.cartItemsTotalCount;
          this.setShoppingCartItemsMessage();
          // AppleCartCount Event
          setTimeout(() => {
            const appleCartCount = new CustomEvent('setAppleCartCount', {
              detail: {
                count: this.cartItemsTotalCount
              }
            });
            window.dispatchEvent(appleCartCount);
          }, 1000);
        },
        error => {
          // TODO: check on getting the value for this
          this.cartTooltipLoadError = true;
          this.emptyCart = true;
        },
        () => {}
      ));
  }

  setShoppingCartItemsMessage(): void {
    // Setup for template variable parsing with lodash. Use custom delimiter {{ }}
    templateSettings.interpolate = /{{([\s\S]+?)}}/g;

    let compileMessage;

    // Interpolate variable in the message
    if (this.cartItems.length === 4) {
      compileMessage = template(this.messages['shoppingCartItem']);
    } else {
      compileMessage = template(this.messages['shoppingCartItems']);
    }

    // Final parsed message to display in the template
    this.shoppingCartItemsMessage = compileMessage({ howManyMore: this.cartItems.length - 3 });
  }

  goToCart(bagMenu) {
    if (this.user.browseOnly) {
      this.modalsService.openBrowseOnlyComponent();
    } else {
      bagMenu.close();
      this.router.navigate(['/store/cart']);
    }
  }

  closePopover() {
    this.isOpen = false;
    if (this.persistCartAbandonNotification) {
      this.persistCartAbandonNotification = false;
    }
    setTimeout(() => {this.cartIcon?.nativeElement.querySelector('.icon-ShoppingCartIcon')?.focus(); }, 100);
  }

  closeBagMenu(bagMenu) {
    bagMenu.close();
    this.isOpen = false;
    if (this.persistCartAbandonNotification) {
      this.persistCartAbandonNotification = false;
    }
    setTimeout(() => {this.cartIcon?.nativeElement.querySelector('.icon-ShoppingCartIcon')?.focus(); }, 100);
  }

  setPositionForBagPopup(): void {
    const popupElement = document.querySelector('.cartPopup') as HTMLElement;
    if (popupElement) {
      const headerElementHeight = document.getElementsByTagName('header')[0]['offsetHeight'] - 48;
      const subNavHeight = document.getElementById('subnav-container')['offsetHeight'] || 0;
      const appWidth = window.innerWidth || document.documentElement.clientWidth;
      popupElement.style.paddingTop = (this.isMobile && appWidth < 766 ) ? this.sharedService.convertToRemUnit(headerElementHeight - subNavHeight) + 'rem' : '';
      popupElement.getElementsByClassName('popover-arrow')[0]['style'].marginTop = popupElement.style.paddingTop;
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

 }
