<!-- HTML Multi lang code is MUL. Refer: https://en.wikipedia.org/wiki/ISO_639-3#Special_codes -->
<!doctype html>
<html id="ng-app" ng-app="coreApp" lang="mul">
    <head>
        <title>Error</title>
        <meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
        <link type="image/x-icon" href="${applicationScope.imageServer}/apple-gr/vars/default/favicon.ico?v=2" rel="icon" />
        <link href="${applicationScope.imageServer}/apple-gr/vars/default/favicon.ico?v=2" rel="shortcut icon" />
        <link href="${pageContext.request.contextPath}/common/css/b2s.css" rel="stylesheet" type="text/css"/>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.6.0.min.js?<%=application.getAttribute("buildId")%>"></script>
    </head>
    <body bgColor="#ffffff" onload="errorPageOnLoad();">
        <div class="login-error-container" role="main">
            <div>
                <div class="error-msg">
                    <p data-msg="offerNoLongerValid"></p>
                </div>
            </div>
        </div>
    </body>
    <script type="text/javascript">
        var messages = {};
        var errorPageOnLoad = function () {
            var locale = (window.location.href.split('?locale=')[1]) ? window.location.href.split('?locale=')[1] : 'en_US';
            var baseUrl= '../service/publicMessages?locale='+locale+'&code_type=login';
            $.ajax({
                url: baseUrl,
                type: "GET",
                contentType: "application/json",
                dataType: 'json',
                success: function (data) {
                    messages = data;
                    bindMessages();
                },
                error: function (error) {
                }
            });
        };

        var bindMessages = function (){
            var attrs = ['[data-msg]'];
            attrs.forEach(function (attr) {
                $(attr).each(function () {
                    var elem = $(this),
                        msgKey = elem.attr(attr.replace(/[\[\]']+/g, ''));//strip square brackets
                    var msg = messages[msgKey];

                    if (elem.is("input, textarea, select")) {
                        elem.val(msg);
                    } else {
                        elem.html(msg);
                    }
                });
            });
        }
    </script>
</html>
