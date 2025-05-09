// Make it safe to do console.log() always.
(function (con) {
    var method;
    var dummy = function() {};
    var methods = ('assert,count,debug,dir,dirxml,error,exception,group,' +
    'groupCollapsed,groupEnd,info,log,markTimeline,profile,profileEnd,' +
    'time,timeEnd,trace,warn').split(',');
    while (method = methods.pop()) {
        con[method] = con[method] || dummy;
    }
})(window.console = window.console || {});

/**
 * isMobile.js v0.4.0
 *
 * A simple library to detect Apple phones and tablets,
 * Android phones and tablets, other mobile devices (like blackberry, mini-opera and windows phone),
 * and any kind of seven inch device, via user agent sniffing.
 *
 * @author: Kai Mallea (kmallea@gmail.com)
 *
 * @license: http://creativecommons.org/publicdomain/zero/1.0/
 */
(function (global) {

    var apple_phone         = /iPhone/i,
        apple_ipod          = /iPod/i,
        apple_tablet        = /iPad/i,
        android_phone       = /(?=.*\bAndroid\b)(?=.*\bMobile\b)/i, // Match 'Android' AND 'Mobile'
        android_tablet      = /Android/i,
        amazon_phone        = /(?=.*\bAndroid\b)(?=.*\bSD4930UR\b)/i,
        amazon_tablet       = /(?=.*\bAndroid\b)(?=.*\b(?:KFOT|KFTT|KFJWI|KFJWA|KFSOWI|KFTHWI|KFTHWA|KFAPWI|KFAPWA|KFARWI|KFASWI|KFSAWI|KFSAWA)\b)/i,
        windows_phone       = /IEMobile/i,
        windows_tablet      = /(?=.*\bWindows\b)(?=.*\bARM\b)/i, // Match 'Windows' AND 'ARM'
        other_blackberry    = /BlackBerry/i,
        other_blackberry_10 = /BB10/i,
        other_opera         = /Opera Mini/i,
        other_chrome        = /(CriOS|Chrome)(?=.*\bMobile\b)/i,
        other_firefox       = /(?=.*\bFirefox\b)(?=.*\bMobile\b)/i, // Match 'Firefox' AND 'Mobile'
        seven_inch = new RegExp(
            '(?:' +         // Non-capturing group

            'Nexus 7' +     // Nexus 7

            '|' +           // OR

            'BNTV250' +     // B&N Nook Tablet 7 inch

            '|' +           // OR

            'Kindle Fire' + // Kindle Fire

            '|' +           // OR

            'Silk' +        // Kindle Fire, Silk Accelerated

            '|' +           // OR

            'GT-P1000' +    // Galaxy Tab 7 inch

            ')',            // End non-capturing group

            'i');           // Case-insensitive matching

    var match = function(regex, userAgent) {
        return regex.test(userAgent);
    };

    var IsMobileClass = function(userAgent) {
        var ua = userAgent || navigator.userAgent;

        // Facebook mobile app's integrated browser adds a bunch of strings that
        // match everything. Strip it out if it exists.
        var tmp = ua.split('[FBAN');
        if (typeof tmp[1] !== 'undefined') {
            ua = tmp[0];
        }

        // Twitter mobile app's integrated browser on iPad adds a "Twitter for
        // iPhone" string. Same probable happens on other tablet platforms.
        // This will confuse detection so strip it out if it exists.
        tmp = ua.split('Twitter');
        if (typeof tmp[1] !== 'undefined') {
            ua = tmp[0];
        }

        this.apple = {
            phone:  match(apple_phone, ua),
            ipod:   match(apple_ipod, ua),
            tablet: !match(apple_phone, ua) && match(apple_tablet, ua),
            device: match(apple_phone, ua) || match(apple_ipod, ua) || match(apple_tablet, ua)
        };
        this.amazon = {
            phone:  match(amazon_phone, ua),
            tablet: !match(amazon_phone, ua) && match(amazon_tablet, ua),
            device: match(amazon_phone, ua) || match(amazon_tablet, ua)
        };
        this.android = {
            phone:  match(amazon_phone, ua) || match(android_phone, ua),
            tablet: !match(amazon_phone, ua) && !match(android_phone, ua) && (match(amazon_tablet, ua) || match(android_tablet, ua)),
            device: match(amazon_phone, ua) || match(amazon_tablet, ua) || match(android_phone, ua) || match(android_tablet, ua)
        };
        this.windows = {
            phone:  match(windows_phone, ua),
            tablet: match(windows_tablet, ua),
            device: match(windows_phone, ua) || match(windows_tablet, ua)
        };
        this.other = {
            blackberry:   match(other_blackberry, ua),
            blackberry10: match(other_blackberry_10, ua),
            opera:        match(other_opera, ua),
            firefox:      match(other_firefox, ua),
            chrome:       match(other_chrome, ua),
            device:       match(other_blackberry, ua) || match(other_blackberry_10, ua) || match(other_opera, ua) || match(other_firefox, ua) || match(other_chrome, ua)
        };
        this.seven_inch = match(seven_inch, ua);
        this.any = this.apple.device || this.android.device || this.windows.device || this.other.device || this.seven_inch;

        // excludes 'other' devices and ipods, targeting touchscreen phones
        this.phone = this.apple.phone || this.android.phone || this.windows.phone;

        // excludes 7 inch devices, classifying as phone or tablet is left to the user
        this.tablet = this.apple.tablet || this.android.tablet || this.windows.tablet;

        if (typeof window === 'undefined') {
            return this;
        }
    };

    var instantiate = function() {
        var IM = new IsMobileClass();
        IM.Class = IsMobileClass;
        return IM;
    };

    if (typeof module !== 'undefined' && module.exports && typeof window === 'undefined') {
        //node
        module.exports = IsMobileClass;
    } else if (typeof module !== 'undefined' && module.exports && typeof window !== 'undefined') {
        //browserify
        module.exports = instantiate();
    } else if (typeof define === 'function' && define.amd) {
        //AMD
        define('isMobile', [], global.isMobile = instantiate());
    } else {
        global.isMobile = instantiate();
    }

})(this);

/*
 This is a minimal bootstrap module for chat/email widget window management and instantiation.
 For fast loading, this should have a minimum # of dependencies.

 DO NOT USE JQUERY OR ANY OTHER LIBRARIES!
 */
var Five9Modules = {};

var Five9SocialWidget = (function() {
    // modules
    var SharedProactive;
    var ChatModel;
    var EmailModel;
    var Persist;

    var validateOptions = function(options) {
        if (typeof options.tenant !== 'string') {
            throw new Error('Must specify a tenant');
        }
        if (typeof options.profiles === 'object' && options.profiles.length) {
            options.profiles = options.profiles.join(',');
        }
        if (typeof options.profiles !== 'string') {
            throw new Error('Must specify profiles');
        }
        options.rootUrl = options.rootUrl || '';
    };
    var getParams = function(options) {
        var params = '';
        for (var key in options) {
            if (options.hasOwnProperty(key)) {
                var val = options[key];
                if (val !== null && val !== undefined && val !== 'undefined' && val !== '' && typeof(val) !== 'function' && key !== 'rootUrl' && key !== 'type' && key !== 'profile') {
                    if (typeof val === 'object') {
                        val = JSON.stringify(val);
                    }
                    params += '&' + key + '=' + encodeURIComponent(val);
                }
            }
        }
        return params.substr(1);
    };
    var loadChatData = function() {
        return Persist.loadData(ChatModel.Key);
    };
    var buildChatUrl = function(options) {
        var rootUrl = options.rootUrl;
        var chatModelSaved = loadChatData();

        if (chatModelSaved && chatModelSaved.session) {
            options.playSoundOnMessage = (chatModelSaved.session.playSoundOnMessage ? chatModelSaved.session.playSoundOnMessage : false);
        }

        var url = encodeURI(rootUrl + 'ChatConsole/index.html?') + getParams(options);

        if (url && url.length > 2000) {
            console.warn('chat url length is: ', url.length);
        }
        else {
            console.log('chat url length is: ', url.length);
        }
        return url;
    };
    var buildEmailUrl = function(options) {
        var rootUrl = options.rootUrl;
        var url = encodeURI(rootUrl + 'EmailConsole/index.html?') + getParams(options);

        if (url && url.length > 2000) {
            console.warn('email url length is: ', url.length);
        }
        else {
            console.log('email url length is: ', url.length);
        }
        return url;
    };
    var buildConsumerPowerUrl = function(options) {
        var rootUrl = options.rootUrl;
        var url = encodeURI(rootUrl + 'ConsumerPowerConsole/index.html?') + getParams(options);

        if (url && url.length > 2000) {
            console.warn('consumer power url length is: ', url.length);
        }
        else {
            console.log('consumer power length is: ', url.length);
        }
        return url;
    };
    var openChat = function(options, pollClose) {
        options = options || {};

        var persistedData = Persist.loadData('f9-chat-console');
        var persistedSessionId;
        var chatOptions = Five9Modules.SharedProactive.sessionData.chatOptions;

        if (persistedData && persistedData.session && persistedData.session.id) {
            persistedSessionId = persistedData.session.id;
        }

        if (chatOptions && chatOptions.sessionId && chatOptions.sessionId === persistedSessionId) {
            options = chatOptions;
        }

        validateOptions(options);
        var url = buildChatUrl(options);
        console.log('openChat', url);
        return openWindow(url, pollClose);
    };
    var openEmail = function(options, pollClose) {
        options = options || {};
        validateOptions(options);
        var url = buildEmailUrl(options);
        console.log('openEmail', url);
        return openWindow(url, pollClose);
    };
    var openConsumerPower = function(options, pollClose) {
        options = options || {};
        validateOptions(options);
        var url = buildConsumerPowerUrl(options);
        console.log('openConsumerPower', url);
        return openWindow(url, pollClose);
    };
    var makeUrlRandom = function(url) {
        var rquery = (/\?/);
        var rts = /([?&])_=[^&]*/;
        var nonce = Date.now();
        var cacheURL = url;
        url = rts.test( cacheURL ) ?
            cacheURL.replace( rts, "$1_=" + nonce++ ) :
        cacheURL + ( rquery.test( cacheURL ) ? "&" : "?" ) + "_=" + nonce++;
        return url;
    };
    var loadCssFile = function(cssPath, cb) {
        cssPath = makeUrlRandom(cssPath);

        var link  = document.createElement('link');
        link.rel  = 'stylesheet';
        link.type = 'text/css';
        link.href = cssPath;
        link.media = 'all';

        if (typeof cb === 'function') {
            link.addEventListener('load', function(e) {
                cb(null, e);
            }, false);
        }
        document.getElementsByTagName('head')[0].appendChild(link);
    };

    var openWindow = function(url, pollClose) {
        /*
         window.open() method has broad browser support.
         however, most browsers have some form of "popup blocker" to allow users to stop the creation of annoying windows

         the general rule is that popup blockers will engage if window.open or similar is invoked from javascript that is not invoked by direct user action. That is, you can call window.open
         in response to a button click without getting hit by the popup blocker, but if you put the same code in a timer event it will be blocked. depth of call chain is also a factor - some
         older browsers only look at the immediate caller, newer browsers can backtrack a little to see if the caller's caller was a mouse click etc.  keep it as shallow as you can to avoid the popup blockers.
         */
        try {
            if (Five9SocialWidget.WindowRef && !Five9SocialWidget.WindowRef.closed) {
                Five9SocialWidget.WindowRef.focus();
                if (pollClose) {
                    startWindowPoll();
                }
                return true;
            }

            var topLeft = popupRight(Five9SocialWidget.WindowWidth, Five9SocialWidget.WindowHeight);
            var props = 'width='  + Five9SocialWidget.WindowWidth + ',height=' + Five9SocialWidget.WindowHeight + ',left=' + topLeft.left + ',top=' + topLeft.top;
            props += ',location=no,menubar=no,resizable=yes,scrollbars=no,status=no,titlebar=no,toolbar=no';
            Five9SocialWidget.WindowRef = window.open('', Five9SocialWidget.WindowName, props);
            if (Five9SocialWidget.WindowRef.location.href === 'about:blank') {
                Five9SocialWidget.WindowRef = window.open(url, Five9SocialWidget.WindowName, props);
            }
            else {
                Five9SocialWidget.WindowRef.focus();
            }
            if (pollClose) {
                startWindowPoll();
            }
            return true;
        }
        catch(err) {
            console.error('Exception during openWindow', err);
            return false;
        }
    };
    var popupCenter = function(w, h) {
        // Fixes dual-screen position                         Most browsers      Firefox
        var dualScreenLeft = window.screenLeft !== undefined ? window.screenLeft : screen.left;
        var dualScreenTop = window.screenTop !== undefined ? window.screenTop : screen.top;

        var width = window.innerWidth ? window.innerWidth : document.documentElement.clientWidth ? document.documentElement.clientWidth : screen.width;
        var height = window.innerHeight ? window.innerHeight : document.documentElement.clientHeight ? document.documentElement.clientHeight : screen.height;

        var left = ((width / 2) - (w / 2)) + dualScreenLeft;
        var top = ((height / 2) - (h / 2)) + dualScreenTop;
        return {top:top,left:left};
    };
    var popupRight = function(w, h) {
        // Fixes dual-screen position                         Most browsers      Firefox
        var dualScreenLeft = window.screenLeft !== undefined ? window.screenLeft : screen.left;
        var dualScreenTop = window.screenTop !== undefined ? window.screenTop : screen.top;

        var width = window.innerWidth ? window.innerWidth : document.documentElement.clientWidth ? document.documentElement.clientWidth : screen.width;
        var height = window.innerHeight ? window.innerHeight : document.documentElement.clientHeight ? document.documentElement.clientHeight : screen.height;

        var left = ((width - 0) - (w / 2)) + dualScreenLeft;
        var top = ((height - 50) - (h / 2)) + dualScreenTop;
        return {top:top,left:left};
    };

    var removeNodeAndListeners = function(el) {
        if (el && el.parentNode) {
            el.src = 'about:blank';
            el.parentNode.removeChild(el);
        }
    };
    var setStyle = function(el, props) {
        if (el) {
            for (var prop in props) {
                el.style[prop] = props[prop];
            }
        }
    };
    var appendTemplate = function(selector, template) {
        var parent = document.body.querySelector(selector);
        var frame = document.createElement('div');
        frame.innerHTML = template;
        parent.appendChild(frame);
        return frame;
    };

    var windowMaximize = function() {
        setStyle(Five9SocialWidget.frame.querySelector('.five9-frame-minimized'), {'display':'none'});
        setStyle(Five9SocialWidget.frame.querySelector('.five9-frame-full'), {'display': 'block','visibility':'visible','height':Five9SocialWidget.WindowHeight + Five9SocialWidget.HeaderHeight +  'px'});
    };
    var windowMinimize = function() {
        setStyle(Five9SocialWidget.frame.querySelector('.five9-frame-minimized'), {'display':'block'});
        setStyle(Five9SocialWidget.frame.querySelector('.five9-frame-full'), {'display': 'none','visibility':'hidden'});
    };
    var addEmbeddedFrame = function(options) {
        console.log('addEmbeddedFrame options: ', options);
        var url = (options.type === 'email') ? buildEmailUrl(options) : ((options.type === 'consumer') ? buildConsumerPowerUrl(options) : buildChatUrl(options));
        console.log('addEmbeddedFrame', url);


        var frame = Five9SocialWidget.frame;
        if (!frame.querySelector('#embedded-frame')) {
            var iframeProps = 'style="border:none;" width="' + Five9SocialWidget.WindowWidth + '" height="' + Five9SocialWidget.WindowHeight + '"';
            appendTemplate('.five9-frame-full', '<iframe id="embedded-frame" src="' + url + '" ' + iframeProps + '></iframe>');
            console.log('WIDGET: add embedded frame');
        }
    };
    var removeEmbeddedFrame = function() {
        var embeddedFrame = Five9SocialWidget.frame.querySelector('#embedded-frame');
        if (embeddedFrame) {
            removeNodeAndListeners(embeddedFrame);
            console.log('WIDGET: remove embedded frame');
        }
    };
    var sendMessageToFrame = function(message) {
        var ref = Five9SocialWidget.data.state === 'popout' ? Five9SocialWidget.WindowRef : Five9SocialWidget.frame.querySelector('#embedded-frame');
        if (ref && ref.contentWindow) {
            ref = ref.contentWindow;
        }
        if (ref && !ref.closed) {
            ref.postMessage(JSON.stringify(message), location.origin);
        }
        else {
            console.warn('Unable to send message to child');
        }
    };
    var loadEmailData = function() {
        return Persist.loadData(EmailModel.Key);
    };
    var loadDetailData = function(options) {
        if (options.type === 'chat') {
            return loadChatData();
        }
        return loadEmailData();
    };

    var setState = function(state) {
        Five9SocialWidget.data.state = state;
        Persist.saveData(Five9SocialWidget.PersistKey, Five9SocialWidget.data);
    };
    var openWindowFromOptions = function(options) {
        if (options.type === 'email') {
            openEmail(options, true);
        }
        else if (options.type === 'consumer') {
            openConsumerPower(options, true);
        }
        else {
            openChat(options, true);
        }
    };
    var onMaximizeClicked = function(options) {
        if (SharedProactive && SharedProactive.inProgress()) {
            console.info('Customer triggered manual chat.  abandon preview chat');
            SharedProactive.abandonPreview();
            SharedProactive.hideChatOffer();
        }

        if (Five9SocialWidget.data.state === 'minimized') {
            console.log('WIDGET: maximize widget');
            if (Five9SocialWidget.isMobile) {
                openWindowFromOptions(options);
                setState('popout');
            }
            else {
                clickMaximize(options);
            }
        }
        else if (Five9SocialWidget.data.state === 'popout') {
            console.log('WIDGET: widget has popout.  attempt to re-use / open window');
            openWindowFromOptions(options);
        }
    };
    var clickMaximize = function(options) {
        addEmbeddedFrame(options);
        windowMaximize();
        setState('maximized');
    };
    var clickMinimize = function() {
        windowMinimize();
        setState('minimized');
    };
    var clickPopout = function(options) {
        windowMinimize();
        removeEmbeddedFrame();
        openWindowFromOptions(options);
        setState('popout');
    };
    var pollWindowOpen = function() {
        // poll window to see if it is closed
        if (Five9SocialWidget.WindowRef && Five9SocialWidget.WindowRef.closed) {
            setState('minimized');
            if (Five9SocialWidget._pollWindowOpenTimerId) {
                clearInterval(Five9SocialWidget._pollWindowOpenTimerId);
                Five9SocialWidget._pollWindowOpenTimerId = null;
            }
            console.log('WIDGET: popout was closed');
        }
    };
    var startWindowPoll = function() {
        Five9SocialWidget._pollWindowOpenTimerId = setInterval(pollWindowOpen, 200);
    };
    var onReceiveMessage = function(e) {
        try {
            if (e && typeof e.data === 'string') {
                var messageData = decodeURIComponent(e.data);
                console.info('Child received message', messageData);

                var message = JSON.parse(messageData);
                if (message) {
                    console.info('child received action [' + message.action + ']');

                    if (message.action === 'minimize') {
                        if (Five9SocialWidget.data.state !== 'popout') {
                            removeEmbeddedFrame();
                        }
                        clickMinimize();
                    }
                    else if (message.action === 'set-localstorage') {
                        Persist.saveData(ChatModel.Key, message.value);
                    }
                }
            }
        }
        catch (err) {
            console.error('onReceiveMessage error', err);
        }
    };
    var enableDrageAndDrop = function() {
        var selected = null,
            x_pos = 0, y_pos = 0,
            x_elem = 0, y_elem = 0;

        function dragInit(elem) {
            selected = elem;
            x_elem = x_pos - selected.offsetLeft;
            y_elem = y_pos - selected.offsetTop;
        };

        function moveElem(e) {
            x_pos = document.all ? window.event.clientX : e.pageX;
            y_pos = document.all ? window.event.clientY : e.pageY;
            if (selected !== null) {
                selected.style.left = (x_pos - x_elem) + 'px';
                selected.style.top = (y_pos - y_elem) + 'px';
            }
        };

        function destroyDragElem() {
            selected = null;
        };

        document.getElementById('five9-frame-full').onmousedown = function () {
            dragInit(this);
            return false;
        };

        document.onmousemove = moveElem;
        document.onmouseup = destroyDragElem;
    };

    return {
        WindowRef: null,
        WindowName: 'Five9SocialWidget',
        WindowWidth: 320,
        WindowHeight: 550,
        HeaderHeight: 40,
        PersistKey: 'f9-social-widget',
        removeEmbeddedFrame: removeEmbeddedFrame,
        data: {
            state: 'minimized',
            type: null
        },

        addWidget: function(options) {
            if (Five9SocialWidget.widgetAdded) return;

            options = options || {};
            validateOptions(options);
            Five9SocialWidget.options = options;

            ChatModel = Five9Modules.ChatModel;
            EmailModel = Five9Modules.EmailModel;
            Persist = Five9Modules.Persist;
            SharedProactive = Five9Modules.SharedProactive;
            if (!ChatModel || !EmailModel || !Persist || !SharedProactive) {
                console.error('Fatal error: missing modules', Five9Modules);
            }
            if (!SharedProactive.supportsFeatures()) {
                return false;
            }

            options.rootUrl = SharedProactive.addTrailingSlash(options.rootUrl);

            var mobile = (isMobile) ? isMobile.any : false;
            Five9SocialWidget.isMobile = mobile;
            Five9SocialWidget.widgetAdded = true;

            window.addEventListener('message', onReceiveMessage, false);

            console.info('addWidget', {
                rootUrl: options.rootUrl,
                isMobile: mobile
            });

            // add widget html
            var buttonClass = (options.type === 'email') ? 'five9-email-button' : 'five9-chat-button';
            var buttonText = (options.type === 'email') ? 'Email' : ((options.type === 'consumer') ? 'Contact Us' : 'Chat');
            var minimizeTooltipText = 'Click to minimize';
            var popoutTooltipText = 'Click to popout into a new window';
            var frameTemplate = '' +
                '<div class="five9-frame-minimized">' +
                '<div class="five9-header">' +
                '<div id="five9-maximize-button" class="' + buttonClass + '">' +
                '<span class="five9-icon"></span><span class="five9-text">' + buttonText + '</span>' +
                '</div>' +
                '</div>' +
                '</div>' +
                '<div id="five9-frame-full" class="five9-frame-full">' +
                '<div class="five9-header">' +
                '<div id="five9-minimize-button" class="' + buttonClass + '">' +
                '<div class="five9-icon"></div><div class="five9-text">' + buttonText + '</div>' +
                '<div id="five9-minimize-icon" title="' + minimizeTooltipText + '"></div>' +
                '<div id="five9-popout-button" title="' + popoutTooltipText + '"></div>' +
                '</div>' +
                '</div>' +
                '</div>';
            var frame = document.createElement('div');
            frame.innerHTML = frameTemplate;
            frame.classList.add('five9-frame');
            frame.setAttribute('ng-show','!isMobile()');

            // add widget css
            if (Five9SocialWidget.cssLoaded) {
                frame.setAttribute('style', 'width: ' + Five9SocialWidget.WindowWidth + 'px;');
            }
            else {
                var cssLoadSuccess = function() {
                    setStyle(frame, {'display':'block'});
                    if (typeof options.done === 'function') {
                        options.done();
                    }
                };

                var cssPath = options.rootUrl + 'SocialWidget/five9-social-widget.css';
                loadCssFile(cssPath, cssLoadSuccess);

                frame.setAttribute('style', 'width: ' + Five9SocialWidget.WindowWidth + 'px;' + 'display:none;');
                Five9SocialWidget.cssLoaded = true;
            }

            document.body.appendChild(frame);
            Five9SocialWidget.frame = frame;

            // respond to persistence
            var data = Persist.loadData(Five9SocialWidget.PersistKey);
            if (data) {
                Five9SocialWidget.data = data;
            }
            Five9SocialWidget.data.type = options.type;

            if (Five9SocialWidget.data.state === 'maximized') {
                var detailData = loadDetailData(options);
                if (detailData && detailData.step === 'Finished') {
                    console.log('WIDGET: widget frame is finished.  no need to re-maximize');
                    setState('minimized');
                }
                else {
                    console.log('WIDGET: widget was maximized.  re-maximize');
                    clickMaximize(options);
                }
            }
            else if (Five9SocialWidget.data.state === 'popout') {
                console.log('WIDGET: widget was popout.  no need to do anything until customer clicks');
            }

            var maximizeButton = frame.querySelector('#five9-maximize-button');
            if (maximizeButton) {
                maximizeButton.addEventListener('click', function(e) {
                    onMaximizeClicked(options);
                });
            }
            var minimizeButton = frame.querySelector('#five9-minimize-icon');
            if (minimizeButton) {
                minimizeButton.addEventListener('click', function(e) {
                    console.log('WIDGET: attempt to minimize widget');
                    detailData = loadDetailData(options);
                    if (options.type === 'consumer') {
                        clickMinimize();
                        console.log('WIDGET: minimize widget');
                    }
                    else if (options.type === 'chat' && ChatModel.allowMinimize(detailData)) {
                        clickMinimize();
                        console.log('WIDGET: minimize widget');
                    }
                    else if (options.type === 'email' && EmailModel.allowMinimize(detailData)) {
                        clickMinimize();
                        console.log('WIDGET: minimize widget');
                    }
                });
            }
            var popoutButton = frame.querySelector('#five9-popout-button');
            if (popoutButton) {
                popoutButton.addEventListener('click', function(e) {
                    e.stopPropagation();
                    clickPopout(options);
                    console.log('WIDGET: popout the widget');
                    return false;
                });
            }

            if (options.allowDragAndDrop) {
                enableDrageAndDrop();
            }
        },
        removeWidget: function() {
            Five9SocialWidget.widgetAdded = false;
            var frame = Five9SocialWidget.frame;
            if (frame) {
                delete Five9SocialWidget.frame;
                removeNodeAndListeners(frame);
            }
        },


        initializeProactiveChat: function(options) {
            if (Five9SocialWidget.proactiveInitialized) return;

            options = options || {};
            validateOptions(options);

            SharedProactive = Five9Modules.SharedProactive;
            if (!SharedProactive) {
                console.error('Fatal error: missing modules', Five9Modules);
            }
            if (!SharedProactive.supportsFeatures()) {
                return false;
            }

            if (!Five9SocialWidget.cssLoaded) {
                var cssPath = options.rootUrl + 'SocialWidget/five9-social-widget.css';
                loadCssFile(cssPath);
                Five9SocialWidget.cssLoaded = true;
            }

            var onOpenChat = function(chatOptions) {
                if (Five9SocialWidget.widgetAdded) {
                    onMaximizeClicked(chatOptions);
                }
                else {
                    openChat(chatOptions, false);
                }
            };
            options.analyticsProvider = 2;
            options.onAccept = onOpenChat;
            options.onReject = function() {};
            SharedProactive.initialize(options);
            Five9SocialWidget.proactiveInitialized = true;
        },
        loadOffers: function(profileName, onSuccess) {
            if (typeof profileName !== 'string') {
                throw new Error('Must specify a profileName');
            }
            onSuccess = onSuccess || function() {};

            var onLoadOffersSuccess = function(response) {
                console.info('onLoadOffersSuccess', response);
                var selectedProfile = null;
                for (var i = 0; i < response.length; i++) {
                    if (response[i].name === profileName) {
                        selectedProfile = response[i];
                        break;
                    }
                }
                if (selectedProfile) {
                    if (selectedProfile.enablePreviewChat) {
                        console.log('Preview chat enabled for ' + profileName);
                    }
                    else {
                        console.log('Preview chat not enabled for ' + profileName);
                    }
                    onSuccess(selectedProfile);
                }
                else {
                    console.error('onLoadOffersSuccess() campaign not found', profileName);
                }
            };
            SharedProactive.loadOffers(onLoadOffersSuccess);
        },
        triggerOffer: function(offerOptions) {
            console.log('five9-social-widget:triggerOffer', offerOptions);
            if (Five9SocialWidget.data.state === 'minimized') {
                SharedProactive.triggerOffer(offerOptions);
                return true;
            }
            else {
                console.warn('Customer is starting a manual chat.  offer not triggered');
                return false;
            }
        },
        maximizeChat: function(chatOptions) {
            if (Five9SocialWidget.widgetAdded) {
                onMaximizeClicked(chatOptions);
            }
            else {
                console.error('Widget must be added to maximize chat');
            }
        },
        processOfferAccepted: function() {
            SharedProactive.triggerCustomerEngageAccept();
        },
        processOfferRefused: function() {
            SharedProactive.triggerCustomerEngageReject();
        }
    };
})();

(function() {
    var ProactivePersist = {
        Version: 1,
        Key: 'f9-offers',

        loadData: function() {
            try {
                var data = sessionStorage.getItem(ProactivePersist.Key);
                if (data) {
                    try {
                        data = JSON.parse(data);
                    }
                    catch (err) {}
                    if (data.version === ProactivePersist.Version) {
                        console.info('Persist:loadData()', ProactivePersist.Key, data);
                        delete data.version;
                        delete data.date;
                        return data;
                    }
                }
            }
            catch(err) {}
            return undefined;
        },
        saveData: function(data) {
            try {
                data.date = Date.now();
                data.version = ProactivePersist.Version;
                sessionStorage.setItem(ProactivePersist.Key, JSON.stringify(data));
                console.info('Persist:saveData()', ProactivePersist.Key, data);
            }
            catch(err) {}
        }
    };

    if (Five9Modules) {
        Five9Modules.ProactivePersist = ProactivePersist;
    }
})();

// SharedProactive is shared with proactive chat and social widget
// it has no dependencies.  DO NOT USE JQUERY OR ANY OTHER LIBRARIES!
(function() {

    // modules
    var MessageTypes;
    var ProactivePersist;
    var NotificationRootSelector = 'five9-notification';
    var NotificationActiveClass = 'active';
    var ModalRootSelector = 'five9-modal';
    var DefaultHeaderText = 'An agent is ready to chat with you';
    var DefaultPreviewAcceptText = 'Respond';
    var DefaultAcceptText = 'Start Live Chat';

    var OnEngageAcceptCallback = function() {};
    var OnEngageRejectCallback = function() {};
    var OnOfferCallback = function() {};

    var getValueFromFields = function(searchKey, fields) {
        for (var key in fields) {
            if (key === searchKey) {
                if (fields[key].value && SharedProactive.nonEmptyString(fields[key].value)) {
                    return fields[key].value;
                }
            }
        }
        return '';
    };
    var formatTime = function(d) {
        // simple date format.  moment, etc is too heavy
        var hours = d.getHours();
        hours = hours % 12 > 0 ? hours % 12 : 12;
        var mins = d.getMinutes();
        mins = mins < 10 ? '0' + mins : mins;
        var a = d.getHours() > 12 ? 'pm' : 'am';
        return hours + ':' + mins + ' ' + a;
    };

    var Requests = {
        loadOffers: function(options, onSuccess, onError) {
            options = options || {};
            if (typeof options.restAPI !== 'string') {
                throw new Error('loadOffers() Must specify a restAPI');
            }
            if (typeof options.tenant !== 'string') {
                throw new Error('loadOffers() Must specify a tenant');
            }

            options.restAPI = SharedProactive.addTrailingSlash(options.restAPI);
            options.rootUrl = SharedProactive.addTrailingSlash(options.rootUrl);

            var restAPI = options.restAPI + SharedProactive.APIPath;
            var url = restAPI + 'orgs/-1/chatoffers/' + options.tenant;
            var xhrOptions = {
                url: url,
                verb: 'GET'
            };
            if (SharedProactive.Mock) {
                setTimeout(function() {
                    onSuccess = onSuccess || function() {};
                    onSuccess([
                        {
                            "name": "user1_inbound_5555550001",
                            "id": 1459464962,
                            "tenantId": "421",
                            "groupId": "1631",
                            "consecutivePagesOnly": 0,
                            "chatOfferCondition": "Amount_of_time_spent",
                            "proactiveChatQuestion": "Do you need help?",
                            "proactiveChatTimeSpent": 10,
                            "proactiveChatHoverDuration": 10,
                            "proactiveChatNumberOfPages": 1,
                            "proactiveChatOfferTimeout": 10,
                            "proactiveChatNumberOfOffer": 30,
                            "proactiveEstimatedWaitTime": 1,
                            "enablePreviewChat": 1,
                            //"enablePreviewChat": 0,
                            //"previewContactEditAllowed": 1
                            "previewContactEditAllowed": 0
                        }
                    ]);
                }, 1000);
            }
            else {
                SharedProactive.xhr(xhrOptions, onSuccess, onError);
            }
        },
        status: function(options, onSuccess, onError) {
            options = options || {};
            if (typeof options.restAPI !== 'string') {
                throw new Error('status() Must specify a restAPI');
            }
            if (options.tenantId === undefined) {
                throw new Error('status() Must specify a tenantId');
            }
            if (options.groupId === undefined) {
                throw new Error('status() Must specify a groupId');
            }

            options.restAPI = SharedProactive.addTrailingSlash(options.restAPI);
            options.rootUrl = SharedProactive.addTrailingSlash(options.rootUrl);

            var restAPI = options.restAPI + SharedProactive.APIPath;
            var url = restAPI + SharedProactive.formatString('orgs/{0}/ewt/group/{1}/{2}', options.tenantId, options.groupId, 1000);
            var xhrOptions = {
                url: url,
                verb: 'GET'
            };
            if (SharedProactive.Mock) {
                setTimeout(function() {
                    onSuccess = onSuccess || function() {};
                    onSuccess({"resultCode":0,"ewt":7,"mediaType":1000,"groupId":1631,"orgId":421});
                }, 1000);
            }
            else {
                SharedProactive.xhr(xhrOptions, onSuccess, onError);
            }
        },
        openSession: function(options, onSuccess, onError) {
            options = options || {};
            if (typeof options.restAPI !== 'string') {
                throw new Error('openSession() Must specify a restAPI');
            }
            if (typeof options.tenant !== 'string') {
                throw new Error('openSession() Must specify a tenant');
            }

            var restAPI = options.restAPI + SharedProactive.APIPath;
            var url = restAPI + 'auth/anon';
            var payload = {
                "tenantName": options.tenant,
                "five9SessionId": null
            };
            console.info('openSession', {url:url,payload:payload});
            var xhrOptions = {
                url: url,
                verb: 'POST',
                payload: payload
            };

            if (SharedProactive.Mock) {
                setTimeout(function() {
                    onSuccess = onSuccess || function() {};
                    onSuccess({
                        'tokenId': 'token-1',
                        'userId': 'token-1',
                        'orgId': 'tenant-1'
                    });
                }, 1000);
            }
            else {
                SharedProactive.xhr(xhrOptions, onSuccess, onError);
            }
        },
        openChatPreview: function(options, onSuccess, onError) {
            options = options || {};
            if (typeof options.restAPI !== 'string') {
                throw new Error('openChatPreview() Must specify a restAPI');
            }
            if (typeof options.sessionId !== 'string') {
                throw new Error('openChatPreview() Must specify a sessionId');
            }
            if (typeof options.profile !== 'string') {
                throw new Error('openChatPreview() Must specify a profile');
            }

            var restAPI = options.restAPI + SharedProactive.APIPath;
            var sessionId = options.sessionId;
            var profile = options.profile;
            var email = getValueFromFields('email', options.fields);
            if (!SharedProactive.nonEmptyString(email)) {
                email = SharedProactive.generateAnonEmail();
                if (options.fields && options.fields.email) {
                    options.fields.email.value = email;
                }
            }
            var name = getValueFromFields('name', options.fields);
            if (!SharedProactive.nonEmptyString(name)) {
                name = '';
            }
            var groupId = options.groupId;
            if (!SharedProactive.nonEmptyString(groupId)) {
                groupId = '-1';
            }
            var getBrowserInfo = function() {
                return JSON.stringify({
                    userAgent: navigator.userAgent,
                    language: navigator.language
                });
            };

            var customFields = [];
            customFields.push({
                'key': 'Subject',
                'value': profile,
                'analyze': 0
            });
            customFields.push({
                'key': 'Name',
                'value': name,
                'analyze': 0
            });
            customFields.push({
                'key': 'Email',
                'value': email,
                'analyze': 0
            });
            customFields.push({
                'key': 'Question',
                'value': '',
                'analyze': 1
            });
            customFields.push({
                'key': 'f9-browser',
                'value': getBrowserInfo(),
                'analyze': 0
            });
            if (options.history) {
                console.info('preview chat history', options.history);
                customFields.push({
                    'key': 'f9-history',
                    'value': JSON.stringify(options.history),
                    'analyze': 0
                });
            }
            else {
                console.info('no history supplied for preview chat');
            }

            if (typeof options.customFields === 'object') {
                customFields = customFields.concat(options.customFields);
            }

            var customVariables = options.customVariables;
            if (typeof customVariables === 'object') {
                for (var id in customVariables) {
                    if (customVariables.hasOwnProperty(id)) {
                        for (var key in customVariables[id]) {
                            if (customVariables[id].hasOwnProperty(key)) {
                                customFields.push({
                                    'key': id + '.' + key,
                                    'value': customVariables[id][key],
                                    'analyze': 0
                                });
                            }
                        }
                    }
                }
            }

            var chatRequest = {};
            chatRequest.campaign = profile;
            chatRequest.groupId = groupId;
            chatRequest.name = name;
            chatRequest.email = email;
            chatRequest.customFields = customFields;
            chatRequest.analyticsProvider = options.analyticsProvider;
            chatRequest.proactiveChat = true;

            var url = restAPI + 'agents/' + sessionId + '/interactions/client_chat_preview';
            console.info('openChatPreview', {url:url,chatRequest:chatRequest});
            var xhrOptions = {
                url: url,
                verb: 'POST',
                payload: chatRequest
            };

            if (SharedProactive.Mock) {
                onSuccess = onSuccess || function() {};
                setTimeout(function() {
                    onSuccess({
                        loggedInProfileAgent: {
                            "profileId": "1631",
                            "profileName": "user1_inbound_5555550001",
                            "agentLoggedIn": true,
                            //"agentLoggedIn": false,
                            "noServiceMessage": "We are currently unable to service your request. Please contact us during normal business hours."
                        },
                        profileSurvey: {
                            'profileId': '2504',
                            'templateId': 3,
                            'templateQuestion': 'Custom survey question',
                            'templateThankyouMessage': 'Custom thank you message',
                            'enableSurvey': 0
                        }
                    });
                }, 1000);
            }
            else {
                SharedProactive.xhr(xhrOptions, onSuccess, onError);
            }
        },
        acceptChatPreviewOffer: function(options, onSuccess, onError) {
            options = options || {};
            if (typeof options.restAPI !== 'string') {
                throw new Error('acceptChatPreviewOffer() Must specify a restAPI');
            }
            if (typeof options.sessionId !== 'string') {
                throw new Error('acceptChatPreviewOffer() Must specify a sessionId');
            }

            var restAPI = options.restAPI + SharedProactive.APIPath;
            var sessionId = options.sessionId;
            var url = restAPI + 'agents/' + sessionId + '/interactions/' + sessionId + '/client_accept_offer';
            var name = getValueFromFields('name', options.fields);
            var email = getValueFromFields('email', options.fields);
            var payload = {
                campaign: options.profile,
                groupId: '-1',
                customFields: [],
                name: name,
                email: email
            };

            console.info('acceptChatPreviewOffer', {url:url,payload:payload});
            var xhrOptions = {
                url: url,
                verb: 'PUT',
                payload: payload
            };
            if (SharedProactive.Mock) {
                setTimeout(function() {
                    onSuccess = onSuccess || function() {};
                    onSuccess({});
                }, 1000);
            }
            else {
                SharedProactive.xhr(xhrOptions, onSuccess, onError);
            }
        },
        rejectChatPreviewOffer: function(options, onSuccess, onError) {
            options = options || {};
            if (typeof options.restAPI !== 'string') {
                throw new Error('rejectChatPreviewOffer() Must specify a restAPI');
            }
            if (typeof options.sessionId !== 'string') {
                throw new Error('rejectChatPreviewOffer() Must specify a sessionId');
            }

            var restAPI = options.restAPI + SharedProactive.APIPath;
            var sessionId = options.sessionId;
            var url = restAPI + 'agents/' + sessionId + '/interactions/' + sessionId + '/client_reject_offer';
            var payload = {};
            console.info('rejectChatPreviewOffer', {url:url,payload:payload});
            var xhrOptions = {
                url: url,
                verb: 'PUT',
                payload: payload
            };
            if (SharedProactive.Mock) {
                setTimeout(function() {
                    onSuccess = onSuccess || function() {};
                    onSuccess({});
                }, 1000);
            }
            else {
                SharedProactive.xhr(xhrOptions, onSuccess, onError);
            }
        }
    };

    var engageChatOffer = function(options) {
        options.onAccept = SharedProactive.triggerCustomerEngageAccept;
        options.onReject = SharedProactive.triggerCustomerEngageReject;
        if (options.notificationType === 'callback') {
            OnOfferCallback(options.question, options.timeout);
        }
        else if (options.notificationType === 'modal') {
            SharedProactive.showChatOfferModal(options);
        }
        else {
            SharedProactive.showChatOfferNotification(options);
        }
    };
    var resume = function() {
        var options = SharedProactive.sessionData.options;
        var chatOptions = SharedProactive.sessionData.chatOptions;
        var selectedProfile = SharedProactive.sessionData.selectedProfile;

        var terminateChat = function(interactionId) {
            var options = SharedProactive.sessionData.options;
            if (options.sessionId === interactionId) {
                console.warn('interaction terminated', interactionId);
                SharedProactive.closeSocket();

                SharedProactive.sessionData.step = SharedProactive.Steps.LoadOffers;
                SharedProactive.save();

                SharedProactive.hideChatOffer();
            }
        };

        var onSocketEngageRequest = function(response) {
            console.info('onSocketEngageRequest', response);

            response = response || {};
            if (response.engaged === 1) {
                if (typeof response.question !== 'string') {
                    console.error('onSocketEngageRequest() error.  API did not return question');
                }

                options.question = response.question;
                SharedProactive.sessionData.step = SharedProactive.Steps.Engaged;
                SharedProactive.save();

                engageChatOffer(options);
            }
            else if (response.engaged === 0) {
                if (this.onServerTerminated && typeof this.onServerTerminated === 'function') {
                    this.onServerTerminated();
                }
                terminateChat(options.sessionId);
                OnEngageRejectCallback();
            }
        };
        var onSocketInteractionTerminated = function(response) {
            response = response || {};
            if (typeof response.interactionId !== 'string') {
                console.error('onSocketInteractionTerminated() error.  API did not return interactionId');
            }
            terminateChat(response.interactionId);
        };
        var onSocketAgentAccept = function(response) {
            response = response || {};
            if (typeof response.interaction !== 'string') {
                console.error('onSocketAgentAccept() error.  API did not return interaction');
            }

            SharedProactive.sessionData.messages[0] = response;

            var interaction = JSON.parse(response.interaction);
            var options = SharedProactive.sessionData.options;
            options.ownerId = interaction.ownerId;
            options.displayName = interaction.displayName;
            SharedProactive.save();
        };

        var onSocketReceived = function(event) {
            if (!event.data) return;
            try {
                var message = JSON.parse(event.data);
                console.log('onSocketReceived', message);
                var payload = message.payLoad;
                var context = message.context;
                if (context && payload) {
                    // TODO_API_HACK depending on several factors (sent via AgentConsole or Freedom API), the messageId can arrive on different objects.  force this to be consistent
                    payload.messageId = context.messageId;
                    if (typeof payload.messageId === 'string' && payload.messageId.length) {
                        // TODO_API_HACK sometimes messageId is string, sometimes an int.  force to an int for consistency
                        payload.messageId = parseInt(payload.messageId, 10);
                    }
                }
                if (payload) {
                    if (payload.messageId === MessageTypes.PREVIEW_ENGAGE_ITEM) {
                        onSocketEngageRequest(payload);
                    }
                    else if (payload.messageId === MessageTypes.INTERACTION_TERMINATE || payload.messageId === MessageTypes.MSG_CHAT_AGENT_TERMINATE) {
                        onSocketInteractionTerminated(payload);
                    }
                    else if (payload.messageId === MessageTypes.MSG_CHAT_AGENT_ACCEPT) {
                        onSocketAgentAccept(payload);
                    }
                }
            }
            catch(err) {
                console.error('Unable to parse socket message', err);
            }
        };
        var onChatPreviewSuccess = function(response) {
            console.info('onChatPreviewSuccess', response);

            if (response && response.loggedInProfileAgent && response.loggedInProfileAgent.agentLoggedIn) {
                chatOptions.preview.survey = response.profileSurvey;
                if (!response.profileSurvey || SharedProactive.nonEmptyString(response.profileSurvey.profileId) === false) {
                    console.error('profileSurvey does not contain a valid profile id');
                }
                chatOptions.preview.profileId = (response.profileSurvey) ? response.profileSurvey.profileId : '';

                SharedProactive.sessionData.step = SharedProactive.Steps.WaitForEngage;
                SharedProactive.save();

                console.info('begin listen for engage');
                SharedProactive.openSocket(options, onSocketReceived);
            }
            else {
                console.warn('No agents are logged in');
            }
        };
        var onSessionSuccess = function(response) {
            console.info('onSessionSuccess', response);
            if (SharedProactive.nonEmptyString(response.tokenId) === false) {
                console.error('session results do not contain a valid tokenId');
            }
            if (SharedProactive.nonEmptyString(response.orgId) === false) {
                console.error('session results do not contain a valid orgId');
            }

            var sessionId = response.tokenId;
            options.sessionId = sessionId;
            chatOptions.sessionId = sessionId;
            chatOptions.preview = {
                tenantId: response.orgId,
                interactionId: sessionId,
                previewContactEditAllowed: selectedProfile.previewContactEditAllowed,
                profile: options.profile
            };

            console.info('sessionId', sessionId);

            SharedProactive.sessionData.step = SharedProactive.Steps.StartChatPreview;
            SharedProactive.sessionData.selectedProfile = selectedProfile;
            SharedProactive.save();

            Requests.openChatPreview(options, onChatPreviewSuccess);
        };

        if (SharedProactive.sessionData.step === SharedProactive.Steps.OpenSession) {
            Requests.openSession(options, onSessionSuccess);
        }
        else if (SharedProactive.sessionData.step === SharedProactive.Steps.StartChatPreview) {
            Requests.openChatPreview(options, onChatPreviewSuccess);
        }
        else if (SharedProactive.sessionData.step === SharedProactive.Steps.WaitForEngage) {
            console.info('begin listen for engage');
            SharedProactive.openSocket(options, onSocketReceived);
        }
        else if (SharedProactive.sessionData.step === SharedProactive.Steps.Engaged) {
            engageChatOffer(options);
        }
    };

    var SharedProactive = {
        Mock: false,
        APIPath: 'appsvcs/rs/svc/',
        Steps: {
            LoadOffers: 'LoadOffers',
            OpenSession: 'OpenSession',
            StartChatPreview: 'StartChatPreview',
            WaitForEngage: 'WaitForEngage',
            Engaged: 'Engaged'
        },
        sessionData: {
            step: 'Unknown',
            offers:[],
            selectedProfile: null,
            options: {},
            chatOptions: {},
            messages: []
        },

        initialize: function(options) {
            options = options || {};
            this.sharedOptions = options;

            if (Five9Modules) {
                ProactivePersist = Five9Modules.ProactivePersist;
                MessageTypes = Five9Modules.MessageTypes;
            }

            if (typeof options.restAPI !== 'string') {
                throw new Error('initialize() Must specify a restAPI');
            }
            if (typeof options.tenant !== 'string') {
                throw new Error('initialize() Must specify a tenant');
            }
            if (typeof options.rootUrl !== 'string') {
                throw new Error('initialize() Must specify a rootUrl');
            }
            if (options.analyticsProvider === undefined) {
                throw new Error('initialize() Must specify an analyticsProvider');
            }

            options.restAPI = SharedProactive.addTrailingSlash(options.restAPI);
            options.rootUrl = SharedProactive.addTrailingSlash(options.rootUrl);

            if (typeof options.onAccept === 'function') {
                OnEngageAcceptCallback = options.onAccept;
            }
            else {
                console.error('onAccept() is required');
            }
            delete options.onAccept;
            if (typeof options.onReject === 'function') {
                OnEngageRejectCallback = options.onReject;
            }
            else {
                console.error('onReject() is required');
            }
            delete options.onReject;
            if (options.notificationType === 'callback' && typeof options.offerCallback !== 'function') {
                console.error('a callback must be supplied');
                options.notificationType = 'notification';
            }
            if (typeof options.offerCallback === 'function') {
                OnOfferCallback = options.offerCallback;
                delete options.offerCallback;
            }

            if (options.useCustomTemplate && (typeof f9CustomPreviewOfferTemplate !== 'undefined' || typeof f9CustomProactiveOfferTemplate !== 'undefined')) {
                NotificationRootSelector = 'five9-notification-custom';
                NotificationActiveClass = 'active-new';

                ModalRootSelector = 'five9-modal-custom';
            }

            console.info('analyticsProvider', options.analyticsProvider);

            var data = ProactivePersist.loadData();
            if (data && data.step !== 'Unknown') {
                SharedProactive.sessionData = data;

                if (data.step === SharedProactive.Steps.LoadOffers) {
                    // wait for trigger
                }
                else {
                    resume();
                }
            }
        },
        supportsFeatures: function() {
            var userAgent = navigator.userAgent;
            if (typeof userAgent.indexOf === 'function') {
                if (userAgent.indexOf('MSIE 7') !== -1 ||
                    userAgent.indexOf('MSIE 8')  !== -1 ||
                    userAgent.indexOf('MSIE 9')  !== -1 ||
                    userAgent.indexOf('MSIE 10') !== -1) {
                    console.error('browser does not support ie10 or below');
                    return false;
                }
            }
            if (!SharedProactive.supportsLocalStorage()) {
                console.error('browser does not support local storage');
                return false;
            }
            if (!SharedProactive.supportsWebSockets()) {
                console.error('browser does not support web sockets');
                return false;
            }
            return true;
        },
        supportsWebSockets: function() {
            return (window.WebSocket && window.WebSocket.CLOSING === 2);
        },
        supportsLocalStorage: function() {
            var mod = 'modernizr';
            try {
                localStorage.setItem(mod, mod);
                localStorage.removeItem(mod);
                return true;
            }
            catch (e) {
                return false;
            }
        },
        save: function() {
            ProactivePersist.saveData(SharedProactive.sessionData);
        },
        loadOffers: function(onSuccess, onError) {
            onSuccess = onSuccess || function() {};
            var options = this.sharedOptions;

            var data = SharedProactive.sessionData;
            if (data && data.step !== 'Unknown') {
                setTimeout(function() {
                    onSuccess(data.offers, false);
                }, 100);
            }
            else {
                var onLoadOffersSuccess = function(response) {
                    var offers = response;
                    SharedProactive.sessionData.step = SharedProactive.Steps.LoadOffers;
                    SharedProactive.sessionData.options = options;
                    SharedProactive.sessionData.offers = offers;
                    for (var i = 0; i < offers.length; i++) {
                        offers[i].numberOfOfferPerSession = offers[i].proactiveChatNumberOfOffer;
                    }
                    SharedProactive.save();

                    onSuccess(offers, true);
                };

                Requests.loadOffers(options, onLoadOffersSuccess, onError);
            }
        },
        inProgress: function() {
            if (!SharedProactive.sessionData || SharedProactive.sessionData.step === 'Unknown' || SharedProactive.sessionData.step === SharedProactive.Steps.LoadOffers) {
                return false;
            }
            return true;
        },
        triggerOffer: function(offerOptions) {
            console.info('triggerOffer', offerOptions);
            offerOptions = offerOptions || {};
            var selectedProfile = offerOptions.profile;
            if (SharedProactive.sessionData.step === 'Unknown' || SharedProactive.sessionData.step === SharedProactive.Steps.LoadOffers) {
                if (typeof selectedProfile !== 'object') {
                    throw new Error('triggerOffer() selectedProfile is required');
                }

                if (offerOptions.onServerTerminated && typeof offerOptions.onServerTerminated === 'function') {
                    this.onServerTerminated = offerOptions.onServerTerminated;
                }

                var options = this.sharedOptions;
                options.history = offerOptions.history;
                options.timeout = selectedProfile.proactiveChatOfferTimeout;
                options.question = selectedProfile.proactiveChatQuestion;
                options.profiles = selectedProfile.name;
                options.groupId = selectedProfile.groupId;
                options.tenantId = selectedProfile.tenantId;
                options.profile = selectedProfile.name;
                options.fields = selectedProfile.fields;

                if (options.timeout === undefined) {
                    console.error('triggerOffer() API timeout undefined');
                }
                if (options.question === undefined) {
                    console.error('triggerOffer() API question undefined');
                }
                if (options.profiles === undefined) {
                    console.error('triggerOffer() API profiles undefined');
                }
                for (var key in offerOptions) {
                    if (options[key] === undefined) {
                        options[key] = offerOptions[key];
                    }
                }

                var chatOptions;
                SharedProactive.sessionData.options = options;
                chatOptions = SharedProactive.shallowClone(options);
                chatOptions.analytics = [options.analyticsProvider,'true'].join(',');
                delete chatOptions.restAPI;
                delete chatOptions.onAccept;
                delete chatOptions.onReject;
                delete chatOptions.question;
                delete chatOptions.timeout;
                delete chatOptions.analyticsProvider;
                SharedProactive.sessionData.chatOptions = chatOptions;

                if (!selectedProfile.enablePreviewChat && chatOptions.preview) {
                    delete SharedProactive.sessionData.chatOptions.preview;
                    SharedProactive.sessionData.chatOptions.profile = selectedProfile;
                    options.preview = false;
                }

                var postStatusTrigger = function() {
                    if (selectedProfile.enablePreviewChat) {
                        // preview chat
                        options.preview = true;
                        options.profile = selectedProfile.name;
                        SharedProactive.sessionData.selectedProfile = selectedProfile;
                        SharedProactive.sessionData.step = SharedProactive.Steps.OpenSession;
                        SharedProactive.save();

                        resume();
                    }
                    else {
                        // regular chat
                        SharedProactive.save();
                        engageChatOffer(options);
                    }
                };
                var onStatusSuccess = function(response) {
                    console.info('onStatusSuccess', response);

                    if (response.resultCode === 0) {
                        var estimatedWaitMinutes = Math.round(response.ewt / 60);
                        console.log('Estimated wait time is : [%d] minutes', estimatedWaitMinutes);
                        if (estimatedWaitMinutes <= selectedProfile.proactiveEstimatedWaitTime) {
                            postStatusTrigger();
                        }
                        else {
                            console.warn('Estimated wait time too long');
                        }
                    }
                    else {
                        console.warn('No agents are logged in');
                    }
                };

                if (options.analyticsProvider === 1) {
                    postStatusTrigger();
                }
                else {
                    console.info('numberOfOfferPerSession', selectedProfile.numberOfOfferPerSession);
                    if (selectedProfile.numberOfOfferPerSession <= 0) {
                        console.warn('Maximum offers per session reached');
                        return;
                    }
                    selectedProfile.numberOfOfferPerSession--;
                    SharedProactive.save();
                    Requests.status(options, onStatusSuccess);
                }
            }
            else {
                console.warn('Proactive chat offer in progress.  aborting new offer');
                return;
            }
        },
        abandonPreview: function() {
            if (SharedProactive.inProgress()) {
                console.info('SharedProactive abandonPreview()');
                SharedProactive.closeSocket();

                SharedProactive.sessionData.step = SharedProactive.Steps.LoadOffers;
                SharedProactive.save();

                Requests.rejectChatPreviewOffer(SharedProactive.sessionData.options);
            }
        },
        triggerCustomerEngageAccept: function() {
            console.info('customer accepted chat request');
            SharedProactive.closeSocket();

            SharedProactive.sessionData.step = SharedProactive.Steps.LoadOffers;
            SharedProactive.save();

            var options = SharedProactive.sessionData.options;
            var chatOptions = SharedProactive.sessionData.chatOptions;
            if (chatOptions.preview) {
                chatOptions.preview.timestamp = Date.now();
                chatOptions.preview.ownerId = options.ownerId;
                chatOptions.preview.displayName = options.displayName;
                chatOptions.preview.messages = SharedProactive.sessionData.messages;
                if (options.question && chatOptions.preview.messages.length) {
                    //it is better to fix on server side, but SCC team asked to fix on UI side, so
                    //replace Welcome Message with an agent initial question
                    var welcomeMessage = chatOptions.preview.messages[0];
                    var parsedInteraction = JSON.parse(welcomeMessage.interaction);
                    parsedInteraction.content = options.question;
                    welcomeMessage.interaction = JSON.stringify(parsedInteraction);
                }

                if (!SharedProactive.nonEmptyString(options.ownerId)) {
                    throw new Error('triggerCustomerEngageAccept() Must specify an ownerId');
                }
                if (typeof options.displayName !== 'string') {
                    throw new Error('triggerCustomerEngageAccept() Must specify an displayName');
                }

                Requests.acceptChatPreviewOffer(options);
            }

            OnEngageAcceptCallback(chatOptions);
        },
        triggerCustomerEngageReject: function() {
            console.info('customer rejected chat request');
            SharedProactive.closeSocket();

            SharedProactive.sessionData.step = SharedProactive.Steps.LoadOffers;
            SharedProactive.save();

            var options = SharedProactive.sessionData.options;
            if (options.preview) {
                Requests.rejectChatPreviewOffer(options);
            }
            OnEngageRejectCallback();
        },

        hideChatOffer: function() {
            console.log('hideChatOffer');
            clearTimeout(SharedProactive.timeoutId);
            var notificationFrame;
            notificationFrame = document.getElementById(NotificationRootSelector);
            if (notificationFrame) {
                notificationFrame.classList.remove(NotificationActiveClass);
            }
            notificationFrame = document.getElementById(ModalRootSelector);
            if (notificationFrame) {
                notificationFrame.classList.remove(NotificationActiveClass);
            }
        },
        showChatOffer: function(options, rootSelector, template) {
            options = options || {};
            if (options.timeout === undefined) {
                throw new Error('showChatOffer() Must specify a timeout');
            }

            var onAccept = options.onAccept || function() {};
            var onReject = options.onReject || function() {};

            var notificationFrame;
            var showNotification = function() {
                notificationFrame = document.getElementById(rootSelector);
                notificationFrame.classList.add(NotificationActiveClass);
            };

            notificationFrame = document.getElementById(rootSelector);

            if (notificationFrame) {
                if (notificationFrame.parentElement) {
                    console.log('removing notificationFrame');
                    notificationFrame.parentElement.removeChild(notificationFrame);
                    notificationFrame = null;
                    Five9SocialWidget.removeEmbeddedFrame();
                    document.body.removeChild(document.getElementById('five9-notification-container'));
                }
            }

            if (!notificationFrame) {
                notificationFrame = document.createElement('div');
                notificationFrame.id = 'five9-notification-container';
                notificationFrame.innerHTML = template;
                document.body.appendChild(notificationFrame);

                var acceptButton = notificationFrame.querySelector('#five9_offerAccepted');
                if (acceptButton) {
                    acceptButton.addEventListener('click', function(e) {
                        SharedProactive.hideChatOffer();
                        onAccept();
                    });
                }
                var refuseMethod = function(e) {
                    SharedProactive.hideChatOffer();
                    onReject();
                };
                var refuseButton = notificationFrame.querySelector('#five9_offerExit');
                if (refuseButton) {
                    refuseButton.addEventListener('click', refuseMethod);
                }
                refuseButton = notificationFrame.querySelector('#five9_offerRefused');
                if (refuseButton) {
                    refuseButton.addEventListener('click', refuseMethod);
                }
            }

            setTimeout(function() {
                showNotification();
            }, 100);
            if (options.timeout) {
                SharedProactive.timeoutId = setTimeout(function() {
                    SharedProactive.hideChatOffer();
                    onReject();
                }, options.timeout * 1000);
            }
        },
        showChatOfferNotification: function(options) {
            options = options || {};
            if (typeof options.question !== 'string') {
                throw new Error('showChatOfferNotification() Must specify a question');
            }

            var headerText;
            var messageText = '';
            var proactiveClass = 'five9-proactive five9-notification';
            var defaultAcceptText;
            var displayName = '';

            if (options.preview) {
                headerText = options.header || DefaultHeaderText;
                messageText = options.question;
                proactiveClass += ' five9-inverse';
                defaultAcceptText = DefaultPreviewAcceptText;
                if (options.displayName) {
                    var now = formatTime(new Date());
                    displayName = options.displayName + ' <span class="display-time">' + now + '</span>';
                }
            }
            else {
                headerText = options.question;
                messageText = '';
                defaultAcceptText = DefaultAcceptText;
            }

            var acceptText = options.acceptText || defaultAcceptText;
            var closeText = options.closeText || 'I\'m OK for now, thanks.';
            var template;


            if (options.useCustomTemplate && options.preview && typeof f9CustomPreviewOfferTemplate !== 'undefined') {
                template = f9CustomPreviewOfferTemplate;
            }
            else if (options.useCustomTemplate && !options.preview && typeof f9CustomProactiveOfferTemplate !== 'undefined') {
                template = f9CustomProactiveOfferTemplate;
            }
            else {
                template = '<div id="five9-notification" class="' + proactiveClass + '">';
                template += '<span class="five9-icon"></span>';
                template += '<span id="five9_offerExit" class="five9-exit"></span>';
                template += '<span class="five9-text">' + headerText + '</span>';
                template += '<div class="five9-agent-text">' + displayName + '</div>';
                template += '<div class="five9-message-text">' + messageText + '</div>';
                template += '<div id="five9_offerAccepted" class="five9-start-button">' + acceptText+ '</div>';
                template += '<div id="five9_offerRefused" class="five9-close-button">' + closeText + '</div>';
                template += '</div>';
            }

            this.showChatOffer(options, NotificationRootSelector, template);
        },
        showChatOfferModal: function(options) {
            options = options || {};
            if (typeof options.question !== 'string') {
                throw new Error('showChatOfferModal() Must specify a question');
            }

            var headerText;
            var messageText = '';
            var proactiveClass = 'five9-proactive five9-modal';
            var defaultAcceptText;
            var displayName = '';

            if (options.preview) {
                headerText = options.header || DefaultHeaderText;
                messageText = options.question;
                proactiveClass += ' five9-inverse';
                defaultAcceptText = DefaultPreviewAcceptText;
                if (options.displayName) {
                    var now = formatTime(new Date());
                    displayName = options.displayName + ' <span class="display-time">' + now + '</span>';
                }
            }
            else {
                headerText = options.question;
                messageText = '';
                defaultAcceptText = DefaultAcceptText;
            }

            var acceptText = options.acceptText || 'Start Live Chat';
            var closeText = options.closeText || 'I\'m OK for now, thanks.';

            var template;
            if (options.useCustomTemplate && options.preview) {
                template = '<div id="five9-modal-custom" class=""><div class="five9-overlay-custom">' + f9CustomPreviewOfferTemplate + '</div></div>';
            }
            else if (options.useCustomTemplate && !options.preview) {
                template = '<div id="five9-modal-custom" class=""><div class="five9-overlay-custom">' + f9CustomProactiveOfferTemplate + '</div></div>';
            }
            else {
                template = '<div id="five9-modal" class="">';
                template += '<div class="five9-overlay">';
                template += '<div class="' + proactiveClass + '">';
                template += '<span class="five9-icon"></span>';
                template += '<span id="five9_offerExit" class="five9-exit"></span>';
                template += '<span class="five9-text">' + headerText + '</span>';
                template += '<div class="five9-agent-text">' + displayName + '</div>';
                template += '<div class="five9-message-text">' + messageText + '</div>';
                template += '<div id="five9_offerAccepted" class="five9-start-button">' + acceptText+ '</div>';
                template += '<div id="five9_offerRefused" class="five9-close-button">' + closeText + '</div>';
                template += '</div>';
                template += '</div>';
                template += '</div>';
            }



            this.showChatOffer(options, ModalRootSelector, template);
        },

        // utility
        xhr: function(options, onSuccess, onError) {
            var url = options.url;
            var verb = options.verb || 'GET';
            var payload = options.payload || null;
            onSuccess = onSuccess || function() {};
            onError = onError || function() {};

            try {
                url = encodeURI(url);
                var xhr = new XMLHttpRequest();
                if (xhr == null) {
                    return;
                }

                var onXHRError = function(err) {
                    var textStatus = 'error';
                    try {
                        textStatus = JSON.parse(xhr.responseText);
                    }
                    catch (err) {}
                    onError(xhr, textStatus, err);
                };

                console.info('xhr(' + url + ')');
                xhr.open(verb, url, true);
                if (verb !== 'GET') {
                    xhr.withCredentials = true;
                }
                xhr.setRequestHeader('Content-Type', 'application/json');
                xhr.onreadystatechange = function() {
                    if (xhr.readyState === 4) {
                        if (xhr.status === 200 || xhr.status === 204) {
                            try {
                                var json = JSON.parse(xhr.responseText);
                                onSuccess(json);
                            }
                            catch (err) {
                                onXHRError(err);
                            }
                        }
                        else {
                            onXHRError();
                        }
                    }
                };
                xhr.onerror = function() {
                    onXHRError();
                };

                if (payload === null) {
                    xhr.send();
                }
                else {
                    xhr.send(JSON.stringify(payload));
                }
            }
            catch (err) {
                onError(err);
            }
        },
        nonEmptyString: function(s) {
            if (typeof s !== 'string') {
                return false;
            }
            return s !== '';
        },
        generateAnonEmail: function() {
            return this.generateGuid() + '@anonymous.com';
        },
        generateGuid: function() {
            // rfc4122 version 4 compliant
            // see http://stackoverflow.com/questions/105034/how-to-create-a-guid-uuid-in-javascript
            // Original solution - http://www.broofa.com/2008/09/javascript-uuid-function/
            // Updated with - http://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
            var d = Date.now();
            var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                var r = (d + Math.random()*16)%16 | 0;
                d = Math.floor(d / 16);
                return (c === 'x' ? r : (r&0x7|0x8)).toString(16);
            });
            return uuid;
        },
        addTrailingSlash: function(s) {
            return (s.substr(-1) === '/') ? s : s + '/';
        },
        shallowClone: function(o) {
            var dest = {};
            for (var key in o) {
                dest[key] = o[key];
            }
            return dest;
        },
        objectToQuery: function(o) {
            var a = [];
            for (var key in o) {
                if (o.hasOwnProperty(key)) {
                    a.push(key + '=' + encodeURIComponent(o[key]));
                }
            }
            var s = '';
            for (var i = 0; i < a.length; i++) {
                s += a[i];
                if (i !== a.length-1) {
                    s += '&';
                }
            }
            return s;
        },
        formatString: function(s) {
            // simple format method.  ex: Utils.format('string {0} test {1}', 'aaa', 'bbb')
            // more interesting string manipulation should use underscore.string
            var args = arguments;
            return s.replace(/\{(\d+)\}/g, function(match, number) {
                number = parseInt(number, 10) + 1;
                if (typeof args[number] === 'undefined') {
                    return undefined;
                }
                var arg = args[number];
                if (typeof arg === 'object') {
                    arg = JSON.stringify(arg);
                }
                return arg;
            });
        },

        openSocket: function(options, onMessage) {
            console.log('openSocket()', options);
            var restAPI = options.restAPI;
            var socketLocation = restAPI.substring(0, restAPI.length-1);
            socketLocation = socketLocation.replace('http://', '');
            socketLocation = socketLocation.replace('https://', '');

            var protocol = 'ws';
            if (restAPI.indexOf('https') !== -1) {
                protocol = 'wss';
            }

            var url = protocol + '://' + socketLocation + '/appsvcs/ws';
            console.info('socket url', url);

            if (SharedProactive.Mock) {
                setTimeout(function() {
                    var data = JSON.stringify({
                        payLoad: {
                            interaction: '{"timestamp":1464976104,"content":"Hello","id":"5b6f6298-29b3-11e6-880d-005056a4db18","ownerId":"138","contentType":1,"displayName":"User One","messageId":"0cf0715b-c7e2-4f9b-aef8-587c9033de71","fromType":1}',
                            interactionId: "5b6f6298-29b3-11e6-880d-005056a4db18",
                            messageId: MessageTypes.MSG_CHAT_AGENT_ACCEPT,
                            status: 1,
                            userId: "405361"
                        }
                    });
                    onMessage({
                        data: data
                    });
                }, 1000);
                setTimeout(function() {
                    onMessage({
                        data: JSON.stringify({
                            payLoad: {
                                question:'FROM AGENT ENGAGE',
                                engaged: 1
                            },
                            context: {
                                messageId: MessageTypes.PREVIEW_ENGAGE_ITEM
                            }
                        })
                    });
                    /*
                     onMessage({
                     data: JSON.stringify({
                     payLoad: {
                     interactionId: SharedProactive.sessionData.options.sessionId
                     },
                     context: {
                     messageId: MessageTypes.INTERACTION_TERMINATE
                     }
                     })
                     });
                     */
                }, 2000);
            }
            else {
                this.socket = new WebSocket(url);
                this.socket.onmessage = onMessage;
            }
        },
        closeSocket: function() {
            console.log('closeSocket()');
            if (this.socket) {
                this.socket.close();
                delete this.socket;
            }
        }
    };

    if (Five9Modules) {
        Five9Modules.SharedProactive = SharedProactive;
    }

})();

(function() {Persist = {
    Version: 1,

    loadData: function(key) {
        try {
            var data = localStorage.getItem(key);
            if (data) {
                try {
                    data = JSON.parse(data);
                }
                catch (err) {}
                if (data.version === Persist.Version && ((key === 'f9-chat-console' && data.step === 'Connecting') || (Date.now() <= (data.date + 170 * 1000)))) {
                    console.info('Persist:loadData()', key, data);
                    delete data.token;
                    delete data.version;
                    delete data.date;
                    return data;
                }
            }
        }
        catch(err) {}
        return undefined;
    },
    saveData: function(key, data) {
        try {
            data.date = Date.now();
            data.version = Persist.Version;
            localStorage.setItem(key, JSON.stringify(data));
            console.info('Persist:saveData()', key, data);
        }
        catch(err) {}
    },
    removeData: function(key) {
        try {
            localStorage.removeItem(key);
            console.info('Persist:removeData()', ProactivePersist.Key, data);
        }
        catch(err) {}
    }
};
    if (Five9Modules) { Five9Modules.Persist = Persist;}})();
(function() {var MessageTypes = {
    /**
     *Temporary to remove
     */
    MSG_TO_IGNORE_1: 1111,
    MSG_TO_IGNORE_2: 1101,
    MSG_TO_IGNORE_3: 1102,

    MSG_BROWSER_NOT_SUPPORTED: -100,
    MSG_CONNECTION_NOT_AVAILABLE: -101,

    RESULT_SUCCESS: 1,
    RESULT_ERROR: -1,


    TRANSFER_TO_GROUP: 1751,
    TRANSFERT_TO_AGENT: 1752,
    AGENT_TRANSFER_AGENT: 1753,

    INTERACTION_TERMINATED: 19507,


    MSG_CHAT_CLIENT_REQUEST: 17001,
    MSG_CHAT_CLIENT_MESSAGE: 17002,
    MSG_CHAT_CLIENT_TERMINATE: 17003,
    MSG_CHAT_CLIENT_MESSAGE_RECEIVED: 17004,
    MSG_CHAT_CLIENT_TYPING: 17005,
    // MSG_CHAT_CLIENT_TRANSFER_TO_GROUP:   17007,
    MSG_NO_SERVICE: 17008,

    PREVIEW_OFFER_ITEM: 19600,
    PREVIEW_ASSIGN_ITEM: 19601,
    PREVIEW_REJECT_ITEM: 19602,
    PREVIEW_ENGAGE_ITEM: 19603,
    PREVIEW_ENGAGE_ACCEPT_ITEM: 19604,
    PREVIEW_ENGAGE_REJECT_ITEM: 19605,

    CUSTOMER_CONTACT_UPDATE: 19608,

    PREVIEW_OFFER_CHERRY_ITEM: 19700,
    PREVIEW_LOCK_CHERRY_ITEM: 19701,

    MSG_CHAT_AGENT_ACCEPT: 18000,
    MSG_CHAT_AGENT_MESSAGE: 18001,
    MSG_CHAT_AGENT_TERMINATE: 18002,
    MSG_CHAT_AGENT_MESSAGE_TO_AGENT: 18004,
    MSG_CHAT_AGENT_TYPING: 18005,
    MSG_CHAT_AGENT_MESSAGE_RECEIVED: 18007,
    MSG_CHAT_AGENT_ADD_AGENT_TO_CHAT: 18008,
    MSG_CHAT_AGENT_REMOVE_AGENT_FROM_CHAT: 18009,
    MSG_CHAT_SIGHTCALL_ESCALATION: 18012,
    MSG_CHAT_SIGHTCALL_VIDEO_ACTIVATED: 18013,
    MSG_CHAT_SIGHTCALL_VIDEO_TERMINATED: 18014,
    MSG_CHAT_SIGHTCALL_CANCELED: 18015,
    MSG_CHAT_AGENT_CONFERENCE_AGENT: 18020,


    MSG_TEXT: 1,
    MSG_HTML: 2,
    MSG_VOICE: 3,
    MSG_VIDEO: 4,
    MSG_FILE: 5,

    STATE_PENDING: 1,
    STATE_DELIVERED: 2,
    STATE_TYPING: 3,
    STATE_DELETING: 4,

    FROM_AGENT: 1,
    FROM_CLIENT: 2,
    FROM_SERVER: 3,
    FROM_TYPING: 4,

    CHAT_STATE_ACTIVE: 1,
    CHAT_STATE_TEMINATED: 2,

    IN: 1,
    OUT: 2,

    getDescription: function(messageId) {
        switch (messageId) {
            case this.MSG_CHAT_CLIENT_REQUEST:
                return "ChatClientRequest";
            case this.MSG_CHAT_CLIENT_MESSAGE:
                return "ChatClientMessage";
            case this.MSG_CHAT_CLIENT_TERMINATE:
                return "ChatClientTerminate";
            case this.MSG_CHAT_CLIENT_MESSAGE_RECEIVED:
                return "ChatClientMessageReceived";
            case this.MSG_CHAT_CLIENT_TYPING:
                return "ChatClientTyping";

            case this.PREVIEW_OFFER_ITEM:
                return 'PREVIEW_OFFER_ITEM';
            case this.PREVIEW_ASSIGN_ITEM:
                return 'PREVIEW_ASSIGN_ITEM';
            case this.PREVIEW_REJECT_ITEM:
                return 'PREVIEW_REJECT_ITEM';
            case this.PREVIEW_ENGAGE_ITEM:
                return 'PREVIEW_ENGAGE_ITEM';
            case this.PREVIEW_ENGAGE_ACCEPT_ITEM:
                return 'PREVIEW_ENGAGE_ACCEPT_ITEM';
            case this.PREVIEW_ENGAGE_REJECT_ITEM:
                return 'PREVIEW_ENGAGE_REJECT_ITEM';

            case this.MSG_CHAT_AGENT_ACCEPT:
                return "ChatAgentAccept";
            case this.MSG_CHAT_AGENT_MESSAGE:
                return "ChatAgentMessage";
            case this.MSG_CHAT_AGENT_TERMINATE:
                return "ChatAgentTerminate";
            case this.MSG_CHAT_AGENT_MESSAGE_TO_AGENT:
                return "ChatAgentToAgent";
            case this.MSG_CHAT_AGENT_TYPING:
                return "ChatAgentTyping";
            case this.MSG_CHAT_AGENT_MESSAGE_RECEIVED:
                return "ChatAgentMessageReceived";
            case this.MSG_CHAT_AGENT_ADD_AGENT_TO_CHAT:
                return "AddAgentToConference";
            case this.MSG_CHAT_AGENT_REMOVE_AGENT_FROM_CHAT:
                return "RemoveAgentFromConference";

            default:
                return "Unknown message id [" + messageId + "]";
        }
    }
};


    /**
     *No longer needed
     */
// MSG_GET_TENANT_ID: 97,
// MSG_GET_USER_LOGGED_IN: 98,
// MSG_GET_PROFILE_SURVEY: 700,
// MSG_CHAT_KEEP_ALIVE: 17000,
// MSG_CHAT_CLIENT_RENEW: 17006,
// MSG_GET_SESSION_INFORMATION: 19000,
    if (Five9Modules) { Five9Modules.MessageTypes = MessageTypes;}})();
(function() {ChatModel = {
    Key: 'f9-chat-console',
    Steps: {
        Unknown: 'Unknown',
        Information: 'Information',
        Connecting: 'Connecting',
        Conversation: 'Conversation',
        Finished: 'Finished'
    },
    PreviewSteps: {
        Unknown: 'Unknown',
        EditContact: 'EditContact',
        Complete: 'Complete'
    },
    SystemFields: [
        'profiles',
        'name',
        'email',
        'question'
    ],

    data: {
        step: 'Unknown',
        previewStep: 'Unknown',
        session: {
        },
        information: {
            profile: '',
            name: '',
            email: '',
            subject: '',
            content: '',
            customFields: {}
        },
        conversation: {
            connectingMessage: '',
            message: '',
            messages: [],
            messageSequence: 1
        }
    },

    getStep: function() {
        return ChatModel.data.step;
    },
    setStep: function(val) {
        ChatModel.data.step = val;
    },
    hasConnectingMessage: function() {
        return ChatModel.data.conversation.connectingMessage !== '';
    },
    getConnectingMessage: function() {
        return ChatModel.data.conversation.connectingMessage;
    },
    setConnectingMessage: function(val) {
        ChatModel.data.conversation.connectingMessage = val;
    },
    getMessages: function() {
        return ChatModel.data.conversation.messages;
    },
    addMessage: function(message) {
        ChatModel.data.conversation.messages.push(message);
    },
    getMessageSequence: function() {
        return ChatModel.data.conversation.messageSequence;
    },
    incrementMessageSequence: function() {
        ChatModel.data.conversation.messageSequence++;
    },
    getPreviewStep: function() {
        return ChatModel.data.previewStep;
    },
    setPreviewStep: function(val) {
        ChatModel.data.previewStep = val;
    },

    addListeners: function() {
        $('#information-page #question').change(ChatModel.saveData);
        $(document.body).on('change', '#information-page .form-control input', ChatModel.saveData);
        $('#conversation-page #input-message').change(ChatModel.saveData);
    },
    modelToView: function() {
        if (ChatModel.data.step !== ChatModel.Steps.Information) {
            gSession.setSession(ChatModel.data.session);
        }

        $('#information-page #name').val(ChatModel.data.information.name);
        $('#information-page #email').val(ChatModel.data.information.email);
        $('#information-page #question').val(ChatModel.data.information.question);
        $('#conversation-page #input-message').val(ChatModel.data.conversation.message);

        var customFields = ChatModel.data.information.customFields;
        if (typeof customFields === 'object') {
            for (var key in customFields) {
                if (customFields.hasOwnProperty(key)) {
                    var val = customFields[key];
                    $('#information-page #' + key).val(val.val);
                }
            }
        }
    },
    viewToModel: function() {
        ChatModel.data.session = gSession.getSession();

        ChatModel.data.information.profile = $('#profiles').val();
        ChatModel.data.information.name = $('#information-page #name').val();
        ChatModel.data.information.email = $('#information-page #email').val();
        ChatModel.data.information.question = $('#information-page #question').val();
        ChatModel.data.conversation.message = $('#conversation-page #input-message').val();

        $('.form-control').each(function() {
            var item = this.id.split('-');
            var key = item[0];
            if ($.inArray(key, ChatModel.SystemFields) === -1) {
                var val = $('input[id="' + key + '"]').val(); // <== $('input#id.with.dot').val() returns undefined!
                if (ChatModel.data.information.customFields) {
                    ChatModel.data.information.customFields[key] = {val:val};
                }
            }
        });
    },
    loadData: function() {
        var data = Persist.loadData(ChatModel.Key);
        if (data && data.step !== ChatModel.Steps.Finished) {
            console.info('Persist: fill views from saved data');
            ChatModel.data = data;
            var messagesData = data.conversation.messages;
            data.conversation.messages = [];
            var messages = data.conversation.messages;
            for (var i = 0; i < messagesData.length; i++) {
                var message = messagesData[i];
                var chatMessage = new ChatMessage(
                    message.id,
                    message.messageId,
                    message.messageType,
                    message.messageDirection,
                    message.messageContent,
                    message.originatorType,
                    message.originatorEmail,
                    message.originatorName,
                    message.destinators,
                    message.contentType,
                    message.confirmation,
                    message.referenceId,
                    message.date
                );
                messages.push(chatMessage);
            }

            ChatModel.modelToView();
            return true;
        }
        return false;
    },
    saveData: function() {
        ChatModel.viewToModel();
        Persist.saveData(ChatModel.Key, ChatModel.data);
        App.sendMessageToParent({action:'set-localstorage',value:ChatModel.data});
    },
    done: function() {
        ChatModel.setStep(ChatModel.Steps.Finished);
        ChatModel.saveData();
    },
    allowMinimize: function(data) {
        //defect: DE23266 - allow customer to minimize chat widget any time
        return true;
    }
};
    if (Five9Modules) { Five9Modules.ChatModel = ChatModel;}})();
(function() {EmailModel = {
    Key: 'f9-email-console',
    Steps: {
        Unknown: 'Unknown',
        Information: 'Information',
        Finished: 'Finished'
    },
    SystemFields: [
        'profiles',
        'name',
        'email',
        'subject'
    ],

    data: {
        step: 'Unknown',
        information: {
            profile: '',
            name: '',
            email: '',
            subject: '',
            content: ''
        },
        hiddenFields: []
    },

    getStep: function() {
        return EmailModel.data.step;
    },
    setStep: function(val) {
        EmailModel.data.step = val;
    },
    setHiddenFields: function (fields) {
        EmailModel.data.hiddenFields = fields;
    },
    addListeners: function() {
        $('#information-page #name').change(EmailModel.saveData);
        $('#information-page #email').change(EmailModel.saveData);
        $('#information-page #subject').change(EmailModel.saveData);
        $('#information-page #content').change(EmailModel.saveData);
    },
    modelToView: function() {
        gEmailEventHandler.setProfileName(EmailModel.data.information.profile);
        $('#information-page #name').val(EmailModel.data.information.name);
        $('#information-page #email').val(EmailModel.data.information.email);
        $('#information-page #subject').val(EmailModel.data.information.subject);
        $('#information-page #content').val(EmailModel.data.information.content);
        if (EmailModel.data.hiddenFields.length) {
            EmailModel.data.hiddenFields.forEach(function (field) {
                $('#'+field+"-form-control").hide();
            });
        }
    },
    viewToModel: function() {
        EmailModel.data.information.profile = $('#profiles').val();
        EmailModel.data.information.name = $('#information-page #name').val();
        EmailModel.data.information.email = $('#information-page #email').val();
        EmailModel.data.information.subject = $('#information-page #subject').val();
        EmailModel.data.information.content = $('#information-page #content').val();
    },
    loadData: function() {
        var data = Persist.loadData(EmailModel.Key);
        if (data && data.step !== EmailModel.Steps.Finished) {
            console.info('Persist: fill views from saved data');
            EmailModel.data = data;
            EmailModel.modelToView();
            return true;
        }
        return false;
    },
    saveData: function() {
        EmailModel.viewToModel();
        Persist.saveData(EmailModel.Key, EmailModel.data);
    },
    done: function() {
        EmailModel.setStep(EmailModel.Steps.Finished);
        EmailModel.saveData();
    },
    allowMinimize: function(data) {
        return true;
    }
};
    if (Five9Modules) { Five9Modules.EmailModel = EmailModel;}})();