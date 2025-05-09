import { ParseNamePipe } from './parse-name-pipe';

describe('ParseNamePipe', () => {

  let parseNamePipe: ParseNamePipe;

  beforeEach(() => {
    parseNamePipe = new ParseNamePipe();
  });

  afterEach(() => {
    parseNamePipe = null;
  });

  it('create an instance', () => {
    expect(parseNamePipe).toBeTruthy();
  });

  it('should parse the string item value', () => {
    expect(parseNamePipe.transform('test the string text')).toBe('testthestr');
  });

  it('should parse the string item value less than 10', () => {
    expect(parseNamePipe.transform('testing')).toBe('testing');
  });
});
