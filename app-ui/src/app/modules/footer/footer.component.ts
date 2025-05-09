import {
  AfterViewInit, ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnInit,
  ViewChild
} from '@angular/core';
import { TemplateStoreService } from '@app/state/template-store.service';
import { FooterDirective } from '@app/modules/footer/footer.directive';
import { FooterService } from '@app/modules/footer/footer.service';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { SharedService } from '@app/modules/shared/shared.service';
import { NavigationEnd, Router, RouterEvent } from '@angular/router';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FooterComponent implements OnInit, AfterViewInit {

  @Input() messages: Messages;
  @Input() config: Config;
  currentYear: number;
  footerTemplate: object;
  footerData: object = {};
  @ViewChild(FooterDirective) appFooter;
  keepAliveOne: string;

  constructor(
    private templateStoreService: TemplateStoreService,
    private footerService: FooterService,
    private cdRef: ChangeDetectorRef,
    private sharedService: SharedService,
    private router: Router,
  ) {
    this.footerData['currentYear'] = new Date().getFullYear();
    this.footerTemplate = templateStoreService.footerTemplate;
  }

  ngOnInit(): void {
    this.router.events.subscribe(
      (event: RouterEvent) => {
        if (event instanceof NavigationEnd) {
          this.cdRef.detectChanges();
        }
      });
  }

  scrollToTop(): boolean {
    window.scroll(0, 0 );
    return false;
  }

  ngAfterViewInit() {
    const viewContainerRef = this.appFooter.viewContainerRef;
    viewContainerRef.clear();
    const componentRef = this.appFooter.viewContainerRef.createComponent(this.footerService.loadFooterComponent( (this.footerTemplate['template']) ? this.footerTemplate['template'] : 'default' )['template']);
    (componentRef.instance as Footer).messages = this.messages;
    (componentRef.instance as Footer).config = this.config;
    (componentRef.instance as Footer).footerData = this.footerData;
    (componentRef.instance as Footer).scrollToTop = this.scrollToTop;
    this.sharedService.keepAliveOne$.subscribe(url => {
      this.keepAliveOne = url;
    });
    this.cdRef.detectChanges();
  }
}
export interface Footer {
  config: Config;
  messages: Messages;
  footerData: object;
  scrollToTop: () => boolean;
}
