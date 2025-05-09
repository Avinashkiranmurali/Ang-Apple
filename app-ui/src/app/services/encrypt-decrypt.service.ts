import { Injectable } from '@angular/core';
import { KEYPHRASE } from '@app/constants/app.constants';
import CryptoES from 'crypto-es';

@Injectable({
  providedIn: 'root'
})

export class EncryptDecryptService {

  private keyPhrase = KEYPHRASE;

  constructor() { }

  encrypt(value: string): string{
    return CryptoES.AES.encrypt(value, this.keyPhrase.trim()).toString();
  }

  decrypt(stringToDecrypt: string){
    return CryptoES.AES.decrypt(stringToDecrypt, this.keyPhrase.trim()).toString(CryptoES.enc.Utf8);
  }
}
