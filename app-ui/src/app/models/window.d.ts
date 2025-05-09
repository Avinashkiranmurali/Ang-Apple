import { Five9SocialWidget } from '@app/services/five9.service';
import { BKTAG } from '@app/components/vars/ua/footer/ua-footer.component';
import { Heap } from '@app/analytics/heap/heap.service';
import { Bootstrapper } from '@app/analytics/ensighten/ensighten';

declare global {
  interface Window {
    WebTrends: () => void;
    Five9SocialWidget: Five9SocialWidget;
    BKTAG: BKTAG;
    parentIFrame;
    iFrameResizer;
    Bootstrapper: Bootstrapper;
    heap: Heap;
    analyticsWindow;
    analyticsUrl;
  }
}
