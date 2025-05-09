<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.StringTokenizer" %>
<%@ page import="org.owasp.esapi.reference.DefaultEncoder" %>

<html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
        <title>Employee Purchasing Program - Login</title>
        <%
            response.setHeader ("Pragma", "No-cache");
            response.setDateHeader ("Expires", 0);
            response.setHeader ("Cache-Control", "no-cache");
            response.addHeader("Cache-Control","no-store");

            String buildDate = "";

            String buildId = (String) application.getAttribute("Build-Id");
            if (buildId!=null) {
                try {
                    String tempBuildDate = buildId.substring(0,buildId.indexOf("_"));
                    StringTokenizer st = new StringTokenizer(tempBuildDate,"-");
                    String year = "";
                    String month = "";
                    String day = "";

                    if (st.countTokens()==3){
                        year = st.nextToken();
                        month = st.nextToken();
                        day = st.nextToken();
                    }

                    String buildtime = buildId.substring(buildId.indexOf("_")+1, buildId.length()).replaceAll("-",":");
                    buildDate = month + "-" + day + "-" + year + "  " + buildtime;
                } catch (Exception e) {
                    //nevermind
                }
            }
            //Invalidate the current session.
            session.invalidate();
        %>
        <link type="image/x-icon" href="${applicationScope.imageServer}/apple-gr/vars/default/favicon.ico?v=2" rel="icon" />
        <link href="${applicationScope.imageServer}/apple-gr/vars/default/favicon.ico?v=2" rel="shortcut icon" />
        <%-- CSS --%>
        <link href="${pageContext.request.contextPath}/common/css/login.css?<%=application.getAttribute("buildId")%>" rel="stylesheet" type="text/css"/>
    </head>
    <body>
        <div class="wrapper">
            <main id="body" class="otp-login">
                <div class="body-container emailForm">
                    <h3 data-msg="welcomeToEPP"></h3>
                    <%--<h4 data-msg="payrollDeductStore"></h4>--%>
                    <p data-msg="enterEmailForAccess"></p>

                    <form name="validateEmail" id="validateEmail">
                        <div>
                            <label for="emailid"><span data-msg="otpEmailAddress"></span>:</label>
                            <input id="emailid" name="emailid" type="text" required autofocus />
                            <p class="error-msg invalid" data-msg="lgnInvalidEmail"></p>
                            <p class="error-msg not-found" data-msg="emailNotFound"></p>
                            <p class="error-msg unknownError" data-msg="unknownError"></p>
                            <p class="error-msg connectionError" data-msg="connectivityError"></p>
                        </div>
                        <div class="submit-button">
                            <input type=submit data-msg="lgnContinue" value="" class="btn-base blue-btn" disabled="disabled" />
                        </div>
                    </form>
                </div>
                <div class="body-container passwordForm">
                    <h3 data-msg="checkEmailForPassword"></h3>

                    <form name="validatePassword" id="validatePassword">
                        <div>
                            <label for="password"><span data-msg="passwordFor"></span> <span id="emailAddr"></span>:</label>
                            <input id="password" name="password" type="password" autocomplete="off" required />
                            <p class="error-msg bad-pass" data-msg="invalidPassword"></p>
                        </div>
                        <div class="submit-button">
                            <input type="submit" data-msg="lgnContinue" value="" class="btn-base blue-btn" disabled="disabled" />
                        </div>
                    </form>
                    <form id="validateLogin" name="validateLogin" action="/apple-gr/ValidateLoginAction.do" method="post" class="hiddenForm">
                        <input type="hidden" id="userid" name="userid" />
                        <input type="hidden" id="pword" name="pword" />
                        <input type="hidden" id="varid" name="varid" />
                        <input type="hidden" id="programid" name="programid" />
                        <input type="hidden" id="OTPLogin" name="OTPLogin" />
                        <input type="hidden" id="email" name="email" />
                        <input type="hidden" id="locale" name="locale" />
                        <%
                            Enumeration<String> enumeration = request.getParameterNames();
                            while(enumeration.hasMoreElements()) {
                                String paramName = enumeration.nextElement();
                                String paramValue = request.getParameter(paramName);
                                final org.owasp.esapi.Encoder esapiEnc = DefaultEncoder.getInstance();
                                final String encPVal = esapiEnc.encodeForHTML(paramValue);
                                final String encPName = esapiEnc.encodeForHTML(paramName);
                                %>
                                    <input type="hidden" id="<%=encPName%>" name="<%=encPName%>" value="<%=encPVal%>"/>
                                <%
                            }
                        %>
                    </form>
                </div>
            </main>
            <footer id="footer">
                <div class="footer-container">
                    <%-- Footer Content --%>
                </div>
            </footer>
        </div>
        <script type="text/javascript" src="${pageContext.request.contextPath}/js/login/login.js?<%=application.getAttribute("buildId")%>"></script>
    </body>
</html>
