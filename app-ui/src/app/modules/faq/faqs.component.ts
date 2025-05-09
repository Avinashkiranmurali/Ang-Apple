import { Component, OnInit } from '@angular/core';
import { TemplateStoreService } from '@app/state/template-store.service';

@Component({
  selector: 'app-faqs',
  templateUrl: './faqs.component.html',
  styleUrls: ['./faqs.component.scss']
})

export class FaqsComponent implements OnInit {
  bodyTemplate: object;
  faqsTemplate: any = {};
  faqsElements: Array<{[key: string]: string}>;
  expanded: object = {};

  constructor(
    private templateStoreService: TemplateStoreService,
  ) {
    this.bodyTemplate = templateStoreService.bodyTemplate;
    this.faqsTemplate = this.bodyTemplate && this.bodyTemplate['faqs'];
    this.faqsElements = this.bodyTemplate['faqs'] && this.bodyTemplate['faqs']['elements'];
  }

  ngOnInit(): void {
  }

}
