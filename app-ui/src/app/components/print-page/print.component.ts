import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-print',
  templateUrl: './print.component.html',
  styleUrls: ['./print.component.scss']
})
export class PrintComponent implements OnInit {
  @Input() printElement: string;
  constructor() { }

  ngOnInit(): void {
  }

  printPage() {
    var mywindow = window.open('', 'PRINT', 'height=400,width=600');
    var container = document.getElementById(this.printElement);

    const printCss = '.dont-print {display: none !important;visibility: hidden !important;}';
    const html = `
      <html>
        <head>
          <style>${printCss}</style>
        </head>
        <body>
          ${container.innerHTML}
        </body>
      </html>
    `;
    mywindow.document.write(html);
    mywindow.document.close();
    mywindow.focus();
    mywindow.print();
    mywindow.close();
  }
}
