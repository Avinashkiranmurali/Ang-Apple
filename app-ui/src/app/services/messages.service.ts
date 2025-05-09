import { Injectable } from '@angular/core';
import { catchError, map } from 'rxjs/operators';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { MessagesStoreService } from '../state/messages-store.service';
import { BaseService } from './base.service';
import { Messages } from '@app/models/messages';

@Injectable({
  providedIn: 'root'
})

export class MessagesService extends BaseService {

  constructor(
    private http: HttpClient,
    private messagesStore: MessagesStoreService
  ) {
    super();
  }

  /**
   *  general and sevice level messages actions
   *  assign the data to the redux pattern messages store
   *  components should only recieve data changes through their store subscribable
   */
  getMessages(): Observable<Messages> {
    const url = this.baseUrl + 'messages';

    return this.http.get<Messages>(url, this.httpOptions)
      .pipe(
        map((response) => {
          this.messagesStore.addMessages(response);
          return response;
        }),
        catchError((error: HttpErrorResponse) => {
          // general and service level error actions
          this.handleError(error);
          return throwError(error);
        })
      );
  }

}
