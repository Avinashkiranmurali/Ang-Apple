import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class LoggerService {

  LoggingLevel = {
    None: 'None',
    Verbose: 'Verbose',
    Info: 'Info',
    Warnings: 'Warnings',
    Errors: 'Errors',
    Log: 'Log'
  };

  constructor() { }

  log(info, level = this.LoggingLevel.Warnings) {
    switch (level) {
      case this.LoggingLevel.Errors:
        /* if (info instanceof HttpErrorResponse) {
          // console.error('HTTP Error: ', info.message, 'Status code:', (info as HttpErrorResponse).status);
        } else if (info instanceof TypeError) {
          // console.error('Type Error: ', info.message);
        } else if (info instanceof Error) {
          // console.error('General Error: ', info.message);
        } else {
          // console.error('Something Happened: ', info);
        } */
        console.error(info);
        break;
      case this.LoggingLevel.Warnings:
        // console.warn(info);
        break;
      case this.LoggingLevel.Info:
        // console.info(info);
        break;
      case this.LoggingLevel.Log:
        // console.log(info);
        break;
      default:
        // console.debug(info);
    }
  }

  logError(error: any) {
    this.log(error, this.LoggingLevel.Errors);
  }

  logWarning(message: any) {
    this.log(message, this.LoggingLevel.Warnings);
  }

  logInfo(message: any) {
    this.log(message, this.LoggingLevel.Info);
  }

  logMessage(message: any) {
    this.log(message, this.LoggingLevel.Log);
  }

  logVerbose(message: any) {
    this.log(message, this.LoggingLevel.Verbose);
  }
}
