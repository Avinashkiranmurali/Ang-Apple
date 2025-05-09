import { AfterViewInit, Component, Input, OnInit, Renderer2, ViewChild, ViewEncapsulation } from '@angular/core';
import { Router } from '@angular/router';
import { AppConstants } from '@app/constants/app.constants';
import { ModalsService } from '@app/components/modals/modals.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { BannerModalComponent } from '@app/components/modals/banner-modal/banner-modal.component';

@Component({
  selector: 'app-template-banner',
  templateUrl: './template-banner.component.html',
  styleUrls: ['./template-banner.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class TemplateBannerComponent implements AfterViewInit {

  @Input() templateBanner: any;
  @Input() categoryId?: string = '';
  @Input() config: any;
  @Input() imageServerUrl?: string;
  @Input() user: any;
  @Input() modalHtml: any;
  @Input() myChasePlanLink: any;
  @ViewChild('banner') banner: any;

  constructor(private router: Router, private renderer: Renderer2,
    private modalService: ModalsService,
    private bootstrapModal: NgbModal) {
  }

  ngAfterViewInit() {
    if (this.banner) {
      const anchors = this.banner.nativeElement.getElementsByTagName('a') as HTMLAnchorElement[];
      for (const anchor of anchors) {
        if (!anchor.hasAttribute('data-external')) {
          this.renderer.listen(anchor, 'click', event => {
            const target = event.target as HTMLAnchorElement;
            const href = target.href;
            const hash = anchor.hash.replace('#', '');
            const fragment = hash ? {fragment: hash} : undefined;
            const pathname = target.pathname;
            event.preventDefault();
            if(pathname==='/'+AppConstants.btnLink){
              this.openBannerModal();
            } else if (pathname.includes('.pdf')){
              window.open(href, target.target ? target.target : '_blank');
            } else {
              this.router.navigate([pathname], fragment);
            }
          });
        }
      }
    }
  }

  openBannerModal() {
    const bannerModal = this.bootstrapModal.open(BannerModalComponent, {
      windowClass: 'banner-modal in',
      backdropClass: 'in',
      centered: true
    });
    bannerModal.componentInstance.config = this.config;
    bannerModal.componentInstance.user = this.user;
    bannerModal.componentInstance.modalHtml = this.modalHtml;
    bannerModal.componentInstance.myChasePlanLink = this.myChasePlanLink;
    bannerModal.componentInstance.categoryId = this.categoryId;
    bannerModal.componentInstance.imageServerUrl = this.imageServerUrl;
    setTimeout(function (){
      document.querySelector( '.modal').scrollTo({top:0,behavior:'smooth'});
    }, 100);
    this.modalService.settop();
  }

}
