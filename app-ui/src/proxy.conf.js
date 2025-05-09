/**
 * To start the proxy with a particular environment set the ENV to environment variable
 *
 * cross-env ENV=XXX ng serve
 */
const modifyResponse = require('node-http-proxy-json');
const ENV = process.env.ENV || 'local443';


const environmentConfig = {
  'local443': { //DEFAULT
    target: 'https://localhost'
  },
  'local8443': {
    target: 'https://localhost:8443'
  },
  'dev': {
    target: 'https://webapp-vip.apldev.bridge2solutions.net/'
  },
  'qa1': {
    target: 'https://webapp-vip.aplqa1.bridge2solutions.net/'
  },
  'qa2': {
    target: 'https://webapp-vip.aplqa2.bridge2solutions.net/'
  },
  'uat': {
    target: 'https://webapp-vip.apluat.bridge2solutions.net/'
  },
  'ang-dev': {
    target: 'https://ang-app-vip.apldev.bridge2solutions.net/'
  },
  'ang-qa': {
    target: 'https://ang-app-vip.aplqa1.bridge2solutions.net/'
  },
  'capbqa': {
    target: 'https://app-vip.capbqa.bridge2solutions.net/'
  }
};


const API_OVERRIDE_CONFIG = {
  '/apple-gr/service/validSession/?spinner=false': {
    transform: function (body) {
      if (body) {
        console.log('OLD Value', body);
        body.keepAliveUrlSource = '/apple-gr/service/keepalive-url-source';
        console.log('NEW Value', body);
      }
      return body;
    }
  }
}

function getTarget() {
  var config = environmentConfig[ENV];
  console.log('ENV:', ENV)
  if (!config) {//People can still give bad configuration
    config = environmentConfig['local443'];//Use default config
    console.log('Loading default config...')
  }
  console.log('config:', config)
  return config.target;
}

// const target = 'https://webapp-vip.apldev.bridge2solutions.net/';
const target = getTarget();


const customRouter = function (req) {

};

// see https://github.com/http-party/node-http-proxy/issues/1165#issuecomment-431025061
// set-cookie was blocked because it was not sent over a secure connection and would have overridden
const onProxyRes = (proxyRes, req, res) => {
  console.log(`Proxying ${req.method} ${req.originalUrl} to ${target} Code: ${res.statusCode}`)
  const sc = proxyRes.headers['set-cookie'];
  const location = proxyRes.headers['location'];
  const token = proxyRes.headers['x-xsrf-token'];
  if (token) {
    console.log('x-xsrf-token:', token);
  }
  // console.log('Response Headers:', proxyRes.headers);
  if (location) {
    // console.log('proxyRes:', proxyRes);
    console.log('location:', location);
    if (location.indexOf('/apple-gr/ui/store') >= 0) {
      proxyRes.headers['location'] = location.replace('/apple-gr/ui/store', '/store');
    } else if (location.indexOf('/apple-gr/ui/maintenance') >= 0) {
      proxyRes.headers['location'] = location.replace('/apple-gr/ui/maintenance', '/maintenance');
    } else if (location.indexOf('/apple-gr/ui/error') >= 0) {
      proxyRes.headers['location'] = location.replace('/apple-gr/ui/error', '/error');
    }
    else if (location.indexOf('/apple-gr/ui/login-error') >= 0) {
      proxyRes.headers['location'] = location.replace('/apple-gr/ui/login-error', '/login-error');
    }
  }
  if (Array.isArray(sc)) {
    proxyRes.headers['set-cookie'] = sc.map(sc => {
      return sc.split(';')
        .filter(v => v.trim().toLowerCase() !== 'secure')
        .filter(v => v.trim().toLowerCase() !== 'httponly')
        .filter(v => v.trim().toLowerCase() !== 'samesite=none')
        .filter(v => v.trim().toLowerCase() !== 'path=/apple-gr')
        .join('; ') + '; Path=/';
    });
    console.log('set-cookie:', proxyRes.headers['set-cookie'])
  }
  modifyResponse(res, proxyRes, function (body) {

    if (API_OVERRIDE_CONFIG[req.originalUrl]) { // If we have a override config
      // modify some information
      return API_OVERRIDE_CONFIG[req.originalUrl].transform(body);
    }
    // If there is no default config return as is.
    return body;
  });


};

const onProxyReq = function (proxyReq, req, res) {
  // add custom header to request
  // console.log('Request Headers:', req.headers)
  // or log the req

};

var PROXY_CONFIG = [{
  target: target,
  // headers: {
  //     host: 'app-vip.apldev.bridge2solutions.net'
  // },
  changeOrigin: true,
  autoRewrite: true,
  secure: false,
  timeout: 360000,
  context: [
    '/apple-gr/**'
  ],
  pathRewrite: {},
  router: customRouter,
  onProxyRes: onProxyRes,
  onProxyReq: onProxyReq,
}, {
  target: 'http://localhost:3000',
  context: ['/imageserver'],
  pathRewrite: {
    '^/imageserver': '/'
  },
}];

module.exports = PROXY_CONFIG;
