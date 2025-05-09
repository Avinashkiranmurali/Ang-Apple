import { ParsePsidPipe } from '@app/pipes/parse-psid.pipe';

describe('ParsePsidPipe', () => {

  let parsePsidPipe: ParsePsidPipe;

  beforeEach(() => {
    parsePsidPipe = new ParsePsidPipe();
  });

  afterEach(() => {
    parsePsidPipe = null;
  });

  it('create an instance', () => {
    expect(parsePsidPipe).toBeTruthy();
  });

  it('should parse the string item value', () => {
    const psid = '30001MXG22LL/A';
    expect(parsePsidPipe.transform(psid, '-')).toBe('30001MXG22LL-A');
  });
});
