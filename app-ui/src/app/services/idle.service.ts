import { Injectable } from '@angular/core';
import { DEFAULT_INTERRUPTSOURCES } from '@ng-idle/core';

@Injectable({
  providedIn: 'root'
})
export class IdleService {

  constructor(
  ) {
  }
  init(idle, keepalive, idleConfig){

    // sets an idle timeout in seconds
    idle.setIdle(idleConfig.idle);
    // sets a timeout period in seconds.
    idle.setTimeout(idleConfig.timeout);
    // sets the default interrupts, in this case, things like clicks, scrolls, touches to the document
    idle.setInterrupts(DEFAULT_INTERRUPTSOURCES);

    idle.onTimeout.subscribe(() => {
      const ev = new CustomEvent('sessionTimeoutEvent', {});
      window.dispatchEvent(ev);
      idle.stop();
    });
    keepalive.interval(idleConfig.interval);

    keepalive.onPing.subscribe(() => {
      // either a generic function or specific to VAR, this function will fire an event upstairs to the parent frame
      if (typeof window['keepalive'] === 'function') {
        window['keepalive']();
      }
    });

    idle.watch();
  }

}
