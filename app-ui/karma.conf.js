// Karma configuration file, see link for more information
// https://karma-runner.github.io/1.0/config/configuration-file.html
// See, https://sybrenbolandit.nl/2019/02/08/chrome-headless-on-jenkins/
const os = require('os');
const path = require('path');
const chromeHeadlessSupported = os.platform() !== 'win32' || Number((os.release().match(/^(\d+)/) || ['0', '0'])[1]) >= 10;
process.env.NO_PROXY = 'localhost, 0.0.0.0/4201, 0.0.0.0/9876';
process.env.no_proxy = 'localhost, 0.0.0.0/4201, 0.0.0.0/9876';
module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('karma-mocha-reporter'),
      require('@angular-devkit/build-angular/plugins/karma'),
      require('karma-sonarqube-reporter')
    ],
    client: {
      captureConsole: false, // leave console.error log
      jasmine: {
        random: false // execute spec in order
      },
      clearContext: false // leave Jasmine Spec Runner output visible in browser
    },
    reporters: ['mocha', 'coverage', 'sonarqube'],
    coverageReporter: {
      dir: require('path').join(__dirname, './reports/coverage/app-ui'),
      subdir: '.',
      reporters: [
        { type: 'html' },
        { type: 'text-summary' },
        { type: 'lcovonly' }
      ],
      fixWebpackSourcePaths: true
    },
    junitReporter: {
      useBrowserName: false
    },
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browserNoActivityTimeout: 30000,
    browsers: [
      chromeHeadlessSupported ? 'ChromeHeadless' : 'Chrome'
    ],
    customLaunchers: {
      ChromeHeadless: {
        base: 'Chrome',
        flags: ['--no-sandbox', '--headless', '--disable-gpu', '--remote-debugging-port=9222']
      }
    },
    //https://www.npmjs.com/package/karma-sonarqube-reporter
    // Default configuration
    sonarqubeReporter: {
      basePath: 'src/app',        // test files folder
      filePattern: '**/*spec.ts', // test files glob pattern
      encoding: 'utf-8',          // test files encoding
      outputFolder: 'reports/unittest',    // report destination
      legacyMode: false,          // report for Sonarqube < 6.2 (disabled)
      reportName: 'report.xml'
    },
    singleRun: true
  });
};
