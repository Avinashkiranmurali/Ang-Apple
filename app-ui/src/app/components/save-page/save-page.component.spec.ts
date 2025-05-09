import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SavePageComponent } from './save-page.component';
import { ElementRef } from '@angular/core';
import * as html2pdf from 'html2pdf.js';

describe('SavePageComponent', () => {
  let component: SavePageComponent;
  let fixture: ComponentFixture<SavePageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SavePageComponent ]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SavePageComponent);
    component = fixture.componentInstance;
    component.content = new ElementRef('<div>Hello, World!</div>');
    component.fileName = 'test.pdf';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
