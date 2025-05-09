import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { TranslateLoader } from '@ngx-translate/core';
import { map } from 'rxjs/operators';
import { MessagesStoreService } from '../state/messages-store.service';
import { Messages } from '@app/models/messages';

@Injectable({
  providedIn: 'root'
})
export class CustomTranslateLoaderService implements TranslateLoader {
  httpOptions: object = {
    observe: 'body'
  };
  baseUrl = '/apple-gr/service/';
  constructor(private http: HttpClient, private messagesStore: MessagesStoreService){}
  url = this.baseUrl + 'messages';
  getTranslation(): Observable<Messages> {
    // this.url += `?locale=zh_HK`;
     return this.http.get<Messages>(this.url, this.httpOptions)
      .pipe(
        map((response) => {
          this.messagesStore.addMessages(response);
          return response;
        })
    );
  }
}




