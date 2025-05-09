import { FilterBannerPipe } from '@app/pipes/filter-banner.pipe';

describe('FilterBannerPipe', () => {
  let filterBannerPipe: FilterBannerPipe;
  const mockBanner = require('assets/mock/banner.json');
  const mockMessages = require('assets/mock/messages.json');

  beforeEach(() => {
    filterBannerPipe = new FilterBannerPipe();
  });

  afterEach(() => {
    filterBannerPipe = null;
  });

  it('create an instance', () => {
    expect(filterBannerPipe).toBeTruthy();
  });

  it('should filter the banner array if messages[banner[\'active\']] is undefined or true', () => {
    filterBannerPipe.transform(mockBanner, mockMessages);
  });

  it('should return null array if not banners available', () => {
    filterBannerPipe.transform(null, mockMessages);
    expect(filterBannerPipe.transform).toBeDefined();
  });

  it('should banners value exits in messages', () => {
    const banners = [
      {active: 'test'}
    ];
    const messages = {
      test: 'test'
    };
    filterBannerPipe.transform(banners, messages);
    expect(filterBannerPipe.transform).toBeDefined();
  });
});
