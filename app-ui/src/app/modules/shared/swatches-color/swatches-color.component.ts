import { Component, Input, OnInit } from '@angular/core';
import { Product } from '@app/models/product';

@Component({
  selector: 'app-swatches-color',
  templateUrl: './swatches-color.component.html',
  styleUrls: ['./swatches-color.component.scss']
})

export class SwatchesColorComponent implements OnInit {
  @Input() item: Product;
  constructor() { }

  ngOnInit(): void { }

}
