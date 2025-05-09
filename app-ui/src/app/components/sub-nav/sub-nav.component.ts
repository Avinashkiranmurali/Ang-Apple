import { Component, ViewChild, ElementRef, Input, HostListener, Renderer2, AfterViewChecked, OnChanges, SimpleChanges } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';
import { Messages } from '@app/models/messages';
import { Category } from '@app/models/category';

@Component({
  selector: 'app-sub-nav',
  templateUrl: './sub-nav.component.html',
  styleUrls: ['./sub-nav.component.scss'],
  animations: [
    trigger('slideMenuAnimation', [

      state('offset', style({
        transform: 'translateX(0px)'
      })),
      state('home', style({
        transform: 'translateX(100px)'
      })),
      transition('home => offset', animate('450ms ease-out'))
    ]),
  ]
})

export class SubNavComponent implements OnChanges {

  @Input() public mainNav: Array<Category>;
  @Input() public subNav: Array<Category>;
  @Input() public currentSubcat: string;
  @Input() public currentSlug: string;
  @Input() public messages: Messages;
  @Input() public categories: Category;
  @Input() public showSubNavBar: boolean;
  @Input() public subNavTextColor: string;
  @Input() public subNavHoverTextColor: string;
  @Input() public newIndicatorColor: string;
  @Input() public displayNewIndicator: boolean;
  @Input() public subNavColors: {};
  @Input() public subNavBackgroundColor: string;

  leftButtonAriaHidden = true;
  rightButtonAriaHidden = true;
  checkScrollButtons = true;
  scrollLength = 150;
  animationState = 'home';
  subNavElement: ElementRef;

  constructor(
    private renderer: Renderer2,
  ) {}

  @ViewChild('leftScroll') leftScroll: ElementRef;
  @ViewChild('rightScroll') rightScroll: ElementRef;
  @ViewChild('navbarPar') navbarPar: ElementRef;
  @ViewChild('subnav') set subnav(element: ElementRef | null){
    if (!element){return; }
    this.subNavElement = element;
    this.displayScrollButtons();
  }

  @HostListener('window:resize')
  onResize(): void {
    if (this.navbarPar && this.subNavElement) {
      // Check if scroll buttons should display after a window resize event?
      this.displayScrollButtons();

      // Re-center the sub-menu on screen resize
      this.renderer.setStyle(this.subNavElement.nativeElement, 'transform', 'translateX(0px)');

      // Re-activate scroll buttons check on window resize
      this.checkScrollButtons = true;
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    // Execute Sub-menu animation on menu change
    this.executeSubMenuAnimation();

    // Re-activate scroll buttons check on menu change
    this.checkScrollButtons = true;
    if (this.navbarPar && this.subNavElement) {
      setTimeout(() => {
        this.displayScrollButtons();
      });
    }
  }


  displayScrollButtons(): void {
    const container = this.navbarPar.nativeElement;
    const containerWidth = container.offsetWidth;
    const list = this.subNavElement.nativeElement;
    const listWidth = list.clientWidth;

    if (containerWidth >= listWidth) {
      this.toggleBtn('left', false);
      this.toggleBtn('right', false);
    } else {
      this.toggleBtn('left', false);
      this.toggleBtn('right', true);
    }
  }

  toggleBtn(btn, display): void {
    const scrollBtn  = (btn === 'left') ? this.leftScroll.nativeElement : this.rightScroll.nativeElement;
    scrollBtn.disabled = !display;
    scrollBtn.setAttribute('aria-hidden', !display);
    this[btn + 'ButtonAriaHidden'] = !display;
  }

  scrollNav(direction): void {
    const container = this.navbarPar.nativeElement;
    const list = this.subNavElement.nativeElement;
    const listOffset = list.getBoundingClientRect();
    const listWidth = list.clientWidth;
    const containerWidth = container.offsetWidth;
    let currentScrollVal = listOffset.left;
    let scrollableArea;
    let remainingToScroll;
    let nextScrollVal;

    if (currentScrollVal > 150) {
      currentScrollVal = 0;
    }

    if (direction === 'right') {
      nextScrollVal = currentScrollVal - this.scrollLength;
      scrollableArea = containerWidth;
      remainingToScroll = nextScrollVal + listWidth;

      nextScrollVal = (remainingToScroll < containerWidth ) ? (nextScrollVal + (scrollableArea - remainingToScroll )) : nextScrollVal;

    } else {
      nextScrollVal = currentScrollVal + this.scrollLength;
      if (nextScrollVal > 0) {
        nextScrollVal = 0;
      }
    }

    const nextScrollValInPixel = nextScrollVal + 'px';
    this.renderer.setStyle(list, 'transform', 'translateX(' + nextScrollValInPixel + ')');

    if (nextScrollVal < 0) {
      this.toggleBtn('left', true);
    } else {
      this.toggleBtn('left', false);
    }

    if (remainingToScroll < scrollableArea) {
      this.toggleBtn('right', false);
    } else {
      this.toggleBtn('right', true);
    }

    // Prevent scroll buttons display check while they are being used
    this.checkScrollButtons = false;
  }

  executeSubMenuAnimation(): void {
    this.animationState = this.animationState === 'offset' ? 'home' : 'offset';
  }

  getNavStateParams(cat: Category, routerState , params): string {
    if (cat.detailUrl) {
      // prepare details state
      let detailsState = 'curated';
      const urlSplit = cat.detailUrl.split('/');
      const param = params || {};
      param.psid = urlSplit[urlSplit.length - 1];

      if (urlSplit.length > 1) {
        param.addCat = urlSplit[urlSplit.length - 2];
      }
      if (param.subcat === param.addCat) {
        detailsState = 'browse';
        detailsState += `/${params.category}/${params.subcat}/${params.psid}`;
      } else {
        detailsState += `/${params.category}/${params.subcat}/${params.addCat}/${params.psid}`;
      }
      return detailsState;
    } else {
      return routerState + `/${params.category}/${params.subcat}`;
    }
  }
}
