import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import merge from 'lodash/merge';
import omit from 'lodash/omit';
import keys from 'lodash/keys';
import { Messages } from '@app/models/messages';

@Injectable({
  providedIn: 'root'
})

export class MessagesStoreService {

  // - We set the initial state in BehaviorSubject's constructor
  // - Nobody outside the Store should have access to the BehaviorSubject
  //   because it has the write rights
  // - Writing to state should be handled by specialized Store methods (ex: addNav, removeNav, etc)
  // - Create one BehaviorSubject per store entity, for example if you have NavGroups
  //   create a new BehaviorSubject for it, as well as the observable$, and getters/setters
  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  private readonly _messages = new BehaviorSubject<Messages>({} as Messages);

  // Expose the observable$ part of the _messages subject (read only stream)
  readonly messages$ = this._messages.asObservable();

  /**
   * the getter will return the last value emitted in _messages subject
   */
  get messages(): Messages {
    return this._messages.getValue();
  }

  /**
   * assigning a value to this.messages will push it onto the observable
   * and down to all of its subsribers (ex: this.messages = [])
   *
   * @param val
   */
  set messages(val: Messages) {
    this._messages.next(val);
  }

  /**
   * we assaign a new copy of messages by adding a new message to it
   * with lodash merge
   * Notice the copy {...} method - used to prepare for onPush Change Detection Strategy
   *
   * @param newMessages
   */
  addMessages(newMessages: Messages) {
    this.messages = merge(this.messages, newMessages);
    this.messages = {...this.messages};
  }

}
