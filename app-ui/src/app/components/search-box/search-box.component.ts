import {
  Component,
  Input,
  OnInit,
  Output,
  EventEmitter,
  OnDestroy,
  HostListener,
  AfterViewInit,
  ViewChildren,
  Injector,
  SimpleChanges,
  OnChanges
} from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { BreakpointObserver } from '@angular/cdk/layout';
import { BreakPoint } from '@app/components/utils/break-point';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { QuickLink } from '@app/models/quick-link';
import { UserStoreService } from '@app/state/user-store.service';
import { ProductService } from '@app/services/product.service';
import { NgbPopover } from '@ng-bootstrap/ng-bootstrap';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-search-box',
  templateUrl: './search-box.component.html',
  styleUrls: ['./search-box.component.scss']
})

export class SearchBoxComponent extends BreakPoint implements OnInit, AfterViewInit, OnDestroy, OnChanges {

  closeTheSearchBox: boolean;
  quickLinksEnabled: boolean;
  showQuickLinks: boolean;
  messages: Messages;
  config: Config;
  searchTerm: string;
  isSearchTextEntered: boolean;
  mobileSearchFocused: boolean;
  searchIsFocused = false;
  private subscriptions: Subscription[] = [];

  @Input() public searchPage: boolean;
  @Input() public terms: string;

  @Input() searchFromNavBar: boolean;

  @Input() quickLinks: QuickLink[];
  @Input() mobileNav: NgbPopover;
  @Output() showQuickLinksChanged: EventEmitter<boolean> = new EventEmitter<boolean>();
  @Output() escKeyPressed: EventEmitter<void> = new EventEmitter<void>();
  @Output() closeSearchEvent: EventEmitter<boolean> = new EventEmitter<boolean>();

  @ViewChildren('searchField') searchField;

  constructor(
    public messageStore: MessagesStoreService,
    public injector: Injector,
    private userStore: UserStoreService,
    private breakpointObserver: BreakpointObserver,
    private productService: ProductService,
    private router: Router
  ) {
    super(injector);
    this.showQuickLinks = false;
    this.messages = this.messageStore.messages;
    this.config = this.userStore.config;
    this.quickLinksEnabled = this.config['quickLinksEnabled'] as boolean;
  }

  ngOnInit(): void {
    this.subscriptions.push(
      this.breakpointObserver.observe(['(max-width:766px)', '(max-width:1027px)']).subscribe(result => {
      if (this.isDesktop || this.isTablet){
        this.showQuickLinks = true;
        this.showQuickLinksChanged.emit(this.showQuickLinks);
      }
    }));

    this.isSearchTextEntered = this.terms > '';

    if (!this.quickLinksEnabled) {
      this.quickLinksEnabled = true;
    }
  }

  ngAfterViewInit(): void {
    if (!this.searchPage && (this.isDesktop || this.isTablet)) {
      this.searchField.first.nativeElement.focus();
    }
    this.popupTabEvent();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.terms && changes.terms.currentValue){
      this.isSearchTextEntered = true;
    }
  }

  focusSearchInput(): void {
    if (this.quickLinksEnabled) {
      this.showQuickLinks = true;
      this.showQuickLinksChanged.emit(this.showQuickLinks);
    }

    if (!(this.isDesktop || this.isTablet)) {
      this.mobileSearchFocused = true;
    }

    this.searchIsFocused = true;
  }

  blurSearchInput(): void {
    this.showQuickLinks = false;
    this.showQuickLinksChanged.emit(this.showQuickLinks);
    this.mobileSearchFocused = false;
    this.terms = '';
    this.isSearchTextEntered = false;
    this.searchIsFocused = false;
  }

  ngOnDestroy(): void {
    this.showQuickLinksChanged.emit(false);
    this.subscriptions.forEach(subscription => subscription.unsubscribe());
  }

  onSearchTextChange(val): void {
    this.isSearchTextEntered = val.target.value.length > 0;
  }

  popupTabEvent(): void {
    setTimeout(() => {
      const popupList = Array.from(document.querySelectorAll('.on-nav-bar, .quick-link-item a'));
      const firstInput = popupList[0] as HTMLElement;
      const lastInput = popupList[ popupList.length - 1 ] as HTMLElement;
      lastInput?.addEventListener('keydown', (event) => { this.redirectToFirst(event, firstInput); });
      firstInput?.addEventListener('keydown', (event) => { this.redirectToLast(event, lastInput); });
    }, 200);
  }

  redirectToFirst(e, firstInput: HTMLElement): void {
    if ((e.which === 9 && !e.shiftKey)) {
      e.preventDefault();
      firstInput.focus();
    }
  }

  redirectToLast(e, lastInput: HTMLElement): void {
    if ((e.which === 9 && e.shiftKey)) {
      e.preventDefault();
      lastInput.focus();
    }
  }

  @HostListener('document:keydown.escape', ['$event']) onKeydownHandler() {
    this.escKeyPressed.emit();
  }

  closeQuickLinks(): void {
    this.escKeyPressed.emit();
    const focusedElement = document.activeElement as HTMLElement;
    focusedElement.blur();
  }

  resetSearchForm(): void {
    this.terms = '';
    this.searchField.first.nativeElement.focus();
    this.isSearchTextEntered = false;
  }

  doSearchSubmit(form: NgForm): void {
    this.searchTerm = form.value.searchTerm;
    const param = { ...(this.searchFromNavBar && {withFacets: false}), keyword: this.terms, order: 'ASCENDING', pageSize: 12, resultOffSet: 0, sort: 'DISPLAY_PRICE' , component: 'search-box', withProducts: false};
    this.subscriptions.push(
    this.productService.getFilteredProducts(param).subscribe(items => {
      const searchRedirect = items.searchRedirect;

      if (searchRedirect) {
        if (searchRedirect.redirectURL) {
          // Go to redirect URL
          this.redirectToUrl(searchRedirect.redirectURL.substring(1));
        }
        else if (searchRedirect.alternateSearchText) {
          // Go to Search Page with alternate search text
          this.redirectToUrl(`/store/search/${searchRedirect.alternateSearchText}`);
        }
      } else {
        setTimeout(() => {
          this.redirectToUrl(`/store/search/${this.searchTerm}`);
        });
      }
    }));
  }

  closeOutSearch(): void {
    if (this.mobileNav) {
      this.mobileNav.close();
    } else {
      this.closeQuickLinks();
    }

    this.onSearchBlur();
  }

  onSearchBlur(): void {
    this.searchIsFocused = false;
  }

  onSearchClose(): void {
    this.escKeyPressed.emit();
    this.closeSearchEvent.emit(this.closeTheSearchBox);
  }

  redirectToUrl(url: string) {
    this.router.navigate([url]).then(e => {
      if (e) {
        this.closeOutSearch();
      }
    });
  }
}
