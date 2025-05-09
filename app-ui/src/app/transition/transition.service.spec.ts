import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { MessagesStoreService } from '@app/state/messages-store.service';
import { TransitionService } from './transition.service';

describe('TransitionService', () => {
  let transitionService: TransitionService;
  const mockMessageData = require('assets/mock/messages.json');
  const dummyElement = document.createElement('div');
  document.getElementById = jasmine.createSpy('HTML Element').and.returnValue(dummyElement);
  const mockRouter = {
    url: 'testUrl',
    navigate: jasmine.createSpy('navigate'),
    parseUrl: () => ({
      root: {
        children: {
          primary: {
            segments: [{ path: 'store/cart'}]
          }
        }
      }
    })
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule
      ],
      providers: [
        { provide: Router, useValue: mockRouter },
        { provide: MessagesStoreService, useValue: { messages: mockMessageData } }
      ]
    });
    transitionService = TestBed.inject(TransitionService);
  });

  it('should be created', () => {
    expect(transitionService).toBeTruthy();
  });

  it('should call openTransition method', () => {
    spyOn(transitionService, 'openTransition').and.callThrough();
    transitionService.openTransition('', '404', false);
    expect(transitionService.openTransition).toHaveBeenCalled();
  });

  it('should call transitionMessageObjectIsSet method', () => {
    spyOn(transitionService, 'transitionMessageObjectIsSet').and.callThrough();
    transitionService.transitionMessageObjectIsSet();
    expect(transitionService.transitionMessageObjectIsSet).toHaveBeenCalled();
  });

  it('should call fadeOutCallback method - for store/cart', () => {
    mockRouter.parseUrl = () => ({
      root: {
        children: {
          primary: {
            segments: [{ path: 'store/cart'}]
          }
        }
      }
    });
    (transitionService as any)._overlayType.next('return');
    spyOn(transitionService, 'fadeOutCallback').and.callThrough();
    transitionService.fadeOutCallback();
    expect(transitionService.fadeOutCallback).toHaveBeenCalled();
  });

  it('should call fadeOutCallback method - default router navigate', () => {
    (window['innerHeight'] as any) = undefined;
    mockRouter.parseUrl = () => ({
      root: {
        children: {
          primary: {
            segments: [{ path: '/testUrl'}]
          }
        }
      }
    });
    (transitionService as any)._overlayType.next('return');
    spyOn(transitionService, 'fadeOutCallback').and.callThrough();
    transitionService.fadeOutCallback();
    expect(transitionService.fadeOutCallback).toHaveBeenCalled();
  });

  it('should call processAriaTransitions method for null', () => {
    spyOn(transitionService, 'processAriaTransitions').and.callThrough();
    transitionService.processAriaTransitions(null);
    expect(transitionService.processAriaTransitions).toHaveBeenCalled();
  });

  it('should call processAriaTransitions method for loading', () => {
    spyOn(transitionService, 'processAriaTransitions').and.callThrough();
    transitionService.processAriaTransitions('loading');
    expect(transitionService.processAriaTransitions).toHaveBeenCalled();
  });

  it('should call openTransition method', () => {
    transitionService['_showOverlay'].next(true);
    spyOn(transitionService, 'openTransition').and.callThrough();
    transitionService.openTransition('loading', '404', true);
    expect(transitionService.openTransition).toHaveBeenCalled();
  });

});
