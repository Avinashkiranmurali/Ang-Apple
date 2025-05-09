import { InterpolatePipe } from './interpolate.pipe';
import { User } from '@app/models/user';
import { Messages } from '@app/models/messages';
import { Program } from '@app/models/program';

describe('InterpolatePipe', () => {
  const user: User = require('assets/mock/user.json');
  const program: Program = require('assets/mock/program.json');
  const messages: Messages = require('assets/mock/messages.json');
  let interpolatePipe: InterpolatePipe;

  beforeEach(() => {
    interpolatePipe = new InterpolatePipe();
  });

  afterEach(() => {
    interpolatePipe = null;
  });

  it('create an instance', () => {
    expect(interpolatePipe).toBeTruthy();
  });

  it('should return the value from messages for Mac', () => {
    const key = 'macFamilyImageTxt';
    expect(interpolatePipe.transform(messages[key], 'user', user)).toEqual('Mac');
  });

  it('should return the value from displayFullNameOnMobile messages', () => {
    const key = 'testing';
    program['config'].displayFullNameOnMobile = false;
    messages.testing = 'Welcome {{ displayFullNameOnMobile ? fullName : firstname }}';
    expect(interpolatePipe.transform(messages[key], 'user', user, program['config'])).toEqual('Welcome Alexandar');
  });
});
