/**
 * To Run this file:
 * node sonar-scan.js --publish --> Will publish to Sonar Server
 * node sonar-scan.js  --> Will not publish to Sonar Server
 * If you want the Jenkins job to fail on erros then add --failonerror to the command line.
 * @type {scan}
 */

const scanner = require('sonarqube-scanner');
const colors = require('colors');
// https://www.npmjs.com/package/minimist
const argv = require('minimist')(process.argv.slice(2));
//https://www.npmjs.com/package/properties-reader
const propertiesReader = require('properties-reader');
const properties = propertiesReader('../gradle.properties');
const request = require('request');

const buildNuber = process.env.BUILD_NUMBER;
const serverUrl = 'http://sonar2.cp.bridge2solutions.net:9000';
const projectKey = 'apple-gr-webapp-anguler-ui';//'com.bridge2.chase.apple%3Achase-apple-sso'

console.log(`BUILD_NUMBER = ${process.env.BUILD_NUMBER}`)
console.log(`GERRIT_BRANCH = ${process.env.GERRIT_BRANCH}`)
console.log(`GERRIT_REFSPEC = ${process.env.GERRIT_REFSPEC}`)
console.log(`GERRIT_CHANGE_SUBJECT = ${process.env.GERRIT_CHANGE_SUBJECT}`)

const projectVersion = `${properties.path().version}-${buildNuber}`;
console.log('Sonar Scan Project Version:', properties.path().version)
console.log('Arguments:', argv)

if (argv.publish) {
  // See https://github.com/bellingard/sonar-scanner-npm
  scanner(
    {
      serverUrl : serverUrl,
      // token : "019d1e2e04eefdcd0caee1468f39a45e69d33d3f",
      options: {
        'sonar.host.url': serverUrl,
        'sonar.projectVersion': projectVersion,
        'sonar.projectKey': projectKey,
        'sonar.projectName': 'apple/webapp-anguler-ui',
        'sonar.sourceEncoding': 'UTF-8',
        'sonar.test.inclusions': '**/*.spec.ts',
        'sonar.sources': './src/app',
        'sonar.exclusions': '**/node_modules/**',
        'sonar.tests': './src/app',
        'sonar.typescript.tsconfigPath': './tsconfig.app.json',
        'sonar.testExecutionReportPaths': 'reports/unittest/report.xml',
        'sonar.typescript.lcov.reportPaths': 'reports/coverage/app-ui/lcov.info'
      }
    },
    check
  )

} else {
  check();
}

function printResult(projectStatus) {
  console.log('Project Status: ');
  console.table(projectStatus.conditions, ['metricKey', 'status', 'actualValue']);
}
function check() {
  checkStatus(projectKey, (projectStatus) => {
    printResult(projectStatus);
    console.log('SUCCESS'.bold.green);
    process.exit()
  }, (projectStatus, err) => {
    printResult(projectStatus);
    console.log('ERROR'.bold.error);
    if (argv.failonerror) {
      process.exit(1);
    } else {
      process.exit();
    }
  })
}

function checkStatus(projectKey, success, failed) {
  const endpoint = `${serverUrl}/api/qualitygates/project_status?projectKey=${projectKey}`
  console.log('Check Status:', endpoint);
  request(endpoint, { json: true }, (err, res, body) => {
    if (err) {
      failed(null, err);
      return console.log(err);
    }
    if (body.projectStatus.status === 'OK') {
      success(body.projectStatus);
    } else {
      failed(body.projectStatus, null);
    }
  });

  //http://sonar2.cp.bridge2solutions.net:9000/api/ce/component?component=apple-gr%3Aapple-gr
  //http://sonar2.cp.bridge2solutions.net:9000/api/project_branches/list?project=apple-gr%3Aapple-gr
  const activityEndpoint = `${serverUrl}/api/project_analyses/search?project=${projectKey}`
  console.log(`activityEndpoint = ${activityEndpoint}`)
  request(activityEndpoint, { json: true }, (err, res, body) => {
    if (err) {
      return console.log(err);
    }
    // console.log('RESPONSE:', body)
    body.analyses.forEach((item, index) => {
      // console.log(item);
    })
  });

}
