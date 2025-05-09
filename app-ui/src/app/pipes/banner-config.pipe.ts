import {Pipe, PipeTransform} from '@angular/core';
import {SharedService} from '@app/modules/shared/shared.service';
import {UserStoreService} from '@app/state/user-store.service';
import {Program} from '@app/models/program';
import {Messages} from '@app/models/messages';
import {MessagesStoreService} from '@app/state/messages-store.service';

@Pipe({
  name: 'bannerConfig'
})
export class BannerConfigPipe implements PipeTransform {

  program: Program;
  messages: Messages

  constructor(
    private sharedService: SharedService,
    private userStore: UserStoreService,
    private messageStore: MessagesStoreService,) {
    this.program = this.userStore.program;
    this.messages = this.messageStore.messages;
  }

  transform(templateBanner: any, config: any, categoryId?: string, imageServerUrl?: string): string {
    if (templateBanner) {
      templateBanner = templateBanner.replaceAll('categoryId', categoryId);
      Object.keys(config).forEach(option => {
        if (option.indexOf('Img') !== -1 || option.indexOf('ImageUrl') !== -1) {
          templateBanner = templateBanner.replaceAll(option, imageServerUrl + config[option]);
        } else if (typeof (config[option]) === 'boolean') {
          const displayText = 'display: ';
          const displayOption = `${displayText}${option}`;
          const isMatchFound = new RegExp(displayOption).test(templateBanner);
          if (isMatchFound) {
            if (!config[option]) {
              templateBanner = templateBanner.replaceAll(displayOption, `${displayText}none`);
            } else {
              templateBanner = templateBanner.replaceAll(displayOption, '');
            }
          }
          templateBanner = templateBanner.replaceAll(option, !config[option]);
        } else {
          const matches = typeof config[option] === 'string' ? config[option].match(/({{.*?}})/g) : null;

          if (matches?.length) {
            matches.forEach((match) => {
              const key = match.replaceAll(/{{|}}/g, '');
              const splitPayOption = this.sharedService.getSplitPayLimitType('dollar');
              const useMaxCash = splitPayOption ? this.sharedService.currencyPipe.transform(splitPayOption.paymentMaxLimit) : (this.sharedService.getSplitPayLimitType('percentage').paymentMaxLimit + '%');
              const programName = this.program.name;
              const pointLabel = this.messages[this.program.formatPointName] ? this.messages[this.program.formatPointName] : '';
              switch (key) {
                case 'useMaxCash':
                  templateBanner = templateBanner.replaceAll(option, config[option]).replaceAll(/{{|}}/g, '').replaceAll(key, useMaxCash);
                  break;
                case 'programName':
                  templateBanner = templateBanner.replaceAll(option, config[option]).replaceAll(/{{|}}/g, '').replaceAll(key, programName);
                  break;
                case 'pointLabel':
                  templateBanner = templateBanner.replaceAll(option, config[option]).replaceAll(/{{|}}/g, '').replaceAll(key, pointLabel.toLowerCase());
                  break;
              }

            });
          } else {
            templateBanner = templateBanner.replaceAll(option, config[option]);
          }
        }
      });
    }
    return templateBanner;
  }

}
