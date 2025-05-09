import {Component, ViewChild, ElementRef, OnInit, Input} from '@angular/core';
import * as html2pdf from 'html2pdf.js';

@Component({
  selector: 'app-save',
  templateUrl: './save-page.component.html',
  styleUrls: ['./save-page.component.scss']
})
export class SavePageComponent implements OnInit {
  @Input() content: ElementRef;
  @Input() fileName: string;
  constructor() {}

  ngOnInit(): void {
  }

  savePage() {
    const options = {
      margin : 0.5,
      filename: this.fileName,
      image: { type: 'jpeg', quality: 1 },
      html2canvas: {
        dpi: 192,
        scale: 4,
        letterRendering: true,
        useCORS: true
      },
      jsPDF: { unit: 'in', format: 'letter', orientation: 'portrait' }
    };

    html2pdf().from(this.content).set(options).save();
  }
}
