import { Component, Input, ViewChild, ChangeDetectorRef, AfterViewInit, ChangeDetectionStrategy } from '@angular/core';
import { TemplateStoreService } from '@app/state/template-store.service';
import { HeaderDirective } from '@app/modules/header/header.directive';
import { HeaderService } from '@app/modules/header/header.service';
import { Messages } from '@app/models/messages';
import { Config } from '@app/models/config';
import { Program } from '@app/models/program';
import { User } from '@app/models/user';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HeaderComponent implements AfterViewInit {

  @Input() messages: Messages;
  @Input() config: Config;
  @Input() program: Program;
  @Input() user: User;
  headerTemplate: object;
  @ViewChild(HeaderDirective) appHeader;

  constructor(
    private templateStoreService: TemplateStoreService,
    private headerService: HeaderService,
    private cdRef: ChangeDetectorRef
  ) {
    this.headerTemplate = templateStoreService.headerTemplate;
  }

  ngAfterViewInit() {
    const viewContainerRef = this.appHeader.viewContainerRef;
    viewContainerRef.clear();
    const componentRef = this.appHeader.viewContainerRef.createComponent(this.headerService.loadHeaderComponent( (this.headerTemplate['template']) ? this.headerTemplate['template'] : 'default' )['template']);
    (componentRef.instance as Header).messages = this.messages;
    (componentRef.instance as Header).config = this.config;
    (componentRef.instance as Header).program = this.program;
    (componentRef.instance as Header).user = this.user;
    (componentRef.instance as Header).headerTemplate = this.headerTemplate;
    this.cdRef.detectChanges();
  }
}

export interface Header {
  config: Config;
  messages: Messages;
  program: Program;
  user: User;
  headerTemplate: object;
}
