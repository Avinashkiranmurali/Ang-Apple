import { SafePipe } from './safe.pipe';
import { DomSanitizer } from '@angular/platform-browser';
import { TestBed, getTestBed } from '@angular/core/testing';

describe('SafePipe', () => {

  let injector;
  let pipe: SafePipe;
  let sanitizer: DomSanitizer;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SafePipe]
    });
    TestBed.compileComponents();

    injector = getTestBed();
    sanitizer = injector.get(DomSanitizer);
    pipe = new SafePipe(sanitizer);

  });

  it('should return safe object for null type as html', () => {
    const obj = {
      value: 'value',
      type: ''
    };
    pipe.transform(obj.value);
  });

  it('safe pipe should return Safe object', () => {
    const options = [
      {
        value: 'value',
        type: 'html'
      },
      {
        value: 'value',
        type: 'style'
      },
      {
        value: 'value',
        type: 'script'
      },
      {
        value: 'value',
        type: 'url'
      },
      {
        value: 'value',
        type: 'resourceUrl'
      }
    ];

    for (const option of options) {
      pipe.transform(option.value, option.type);
    }
  });

  it('should throw an error if the type is none of the above types', () => {
    expect(() => {
      pipe.transform('value', 'invalidUrl');
    }).toThrow(new Error('Invalid safe type specified: invalidUrl'));
  });
});
