import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { Meta } from '@angular/platform-browser';
import {
  Event,
  NavigationCancel,
  NavigationEnd,
  NavigationError,
  NavigationStart,
  Router
} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

export class AppComponent implements OnInit {
  title = 'app-ui';

  loading = false;
  @HostListener('window:resize', ['$event'])
  onResize(event) {
    this.windowResize();
  }
  constructor(private router: Router, private meta: Meta) { }

  ngOnInit() {
    this.windowResize();
  }
  windowResize() {
    if (window.outerWidth >= 768 && window.outerWidth <= 1024) {
      this.meta.updateTag({
        name: 'viewport',
        content: 'width=1024'
      });
    }
    else {
      this.meta.updateTag({
        name: 'viewport',
        content: 'width=device-width, initial-scale=1.0'
      });
    }
  }
}
