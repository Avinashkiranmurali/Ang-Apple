import { AfterViewInit, Component, Input, OnInit, Inject, Renderer2, ViewChild, ElementRef } from '@angular/core';
import { DOCUMENT } from '@angular/common';

declare const UIkit: any;

@Component({
  selector: 'app-slideshow',
  templateUrl: './slideshow.component.html',
  styleUrls: ['./slideshow.component.scss', '../../../../node_modules/uikit/dist/css/uikit.min.css', ]
})
export class SlideshowComponent implements OnInit, AfterViewInit {

  activeClass?: string;

  @Input() banners?: any = [];
  @Input() isMobile?: boolean = false;
  @Input() imageServerUrl?: string;

  @ViewChild('slideShow') slideShow!: ElementRef;

  constructor(
    @Inject(DOCUMENT) private document: Document,
    private renderer: Renderer2) {}

  ngOnInit(): void {
    /* istanbul ignore next */
    UIkit.util.on('#slideShowComponent', 'itemshow',  (e: CustomEvent) => {
      if (this.activeClass) {
        this.renderer.removeClass(this.document.getElementById('slideShowComponent'), this.activeClass);
      }
      this.activeClass = (e.target as HTMLElement).classList[0];
      this.renderer.addClass(this.document.getElementById('slideShowComponent'), this.activeClass);
    });
  }

  ngAfterViewInit() {
    setTimeout(() => {
      const dotnav: NodeListOf<Element> = this.slideShow.nativeElement.querySelectorAll('.uk-dotnav li a');
      dotnav.forEach((el, index) => {
        el.setAttribute('aria-label', `Slide ${(index + 1)}`);
      });
    });
  }
}
