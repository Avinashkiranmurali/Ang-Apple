import { AfterViewChecked, Component, ElementRef, HostListener, Inject, Input, OnInit, Renderer2, ViewChild } from '@angular/core';
import { DOCUMENT } from '@angular/common';

declare const window: any;

@Component({
  selector: 'app-zoomable-slideshow',
  templateUrl: './zoomable-slideshow.component.html',
  styleUrls: ['./zoomable-slideshow.component.scss','../../../../node_modules/uikit/dist/css/uikit.min.css']
})

export class ZoomableSlideshowComponent implements OnInit, AfterViewChecked {
  zoomActivated?: boolean = false;
  @Input() carouselImages?: Array<string> = [];
  @Input() imageServerUrl?: string = '';
  @ViewChild('zoomSlideShow') slideShow!: ElementRef;

  @HostListener('window:resize', ['$event'])
  onResize(): void {
    this.deActivateZoom();
  }
  constructor(
    @Inject(DOCUMENT) private document: Document,
    private renderer: Renderer2) {}

  ngOnInit(): void {}

  activateZoom(id) {
    let i = id;
    this.zoomActivated = true;
    const element = this.document.getElementById('zoomableSlideShowComponent');
    this.renderer.addClass(this.document.body, 'hideScroll');
    window['UIkit'].slider(element)['index'] = parseInt(i || '0');
    setTimeout(() => {
      const closeButton = this.slideShow.nativeElement.querySelector('.uk-close');
      closeButton?.focus();
    });
  }

  deActivateZoom() {
    if (this.zoomActivated) {
      this.zoomActivated = false;
      this.renderer.removeClass(this.document.body, 'hideScroll');
    }
  }

  ngAfterViewChecked() {
    setTimeout(() => {
      const elements: NodeListOf<Element> = this.slideShow.nativeElement.querySelectorAll('li');
      elements.forEach(ele  => {
        ele?.setAttribute('tabindex', '0')
      });
    });
  }
}
