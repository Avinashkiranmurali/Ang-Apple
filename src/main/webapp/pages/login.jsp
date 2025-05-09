<%@ page import="java.util.StringTokenizer" %>
<%@ page import="org.owasp.esapi.ESAPI" %>

<!DOCTYPE html>
<html>
<HEAD>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <TITLE>Please Log On</TITLE>
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
    <script type="text/javascript" src="${pageContext.request.contextPath}/js/jquery-3.6.0.min.js?<%=application.getAttribute("buildId")%>"></script>
    <link href="${pageContext.request.contextPath}/common/css/paged_table.css?<%=application.getAttribute("buildId")%>" rel="stylesheet" type="text/css"/>
    <link href="${pageContext.request.contextPath}/common/css/login.css?<%=application.getAttribute("buildId")%>" rel="stylesheet" type="text/css"/>

    <script type="text/javascript" language="JavaScript">
        function validate_data(obj) {
            var key = document.getElementById('userid').value;

            if (key.length <= 0) {
                alert("Please enter a valid User Id");
                document.validateLogin.userid.focus();
                return false;
            }
            key = document.getElementById('pword').value;
            if (key.length <= 0) {
                alert("Please enter a valid Password");
                document.validateLogin.pword.focus();
                return false;
            }
            key = document.getElementById('programid').value;
            if (key.length <= 0) {
                alert("Please enter a valid Program Id");
                document.validateLogin.programid.focus();
                return false;
            }

            key = document.getElementById('varid').value;
            if (key.length <= 0) {
                alert("Please enter a valid Client Id");
                document.validateLogin.varid.focus();
                return false;
            }

            var varId = document.getElementById('varid').value,
                    programId = document.getElementById('programid').value,
                    isVitality = varId.search('Vitality'),
                    isTVG = (programId === 'TVGCorporate'),
                    pricingId = (isVitality >= 0) && isTVG ? $("#pricingIdSelect").val() : (isVitality >= 0) && !isTVG ? 'N/A' : '';

            if ((pricingId !== null && pricingId !== '')) {
                $('<input>').attr({
                    type: 'hidden',
                    id: 'pricingId',
                    name: 'pricingId',
                    value: pricingId
                }).appendTo('#validateLogin')
            }

            var deceased = document.getElementById("deceased").checked;

            if (varId.toUpperCase() !== "WF" && deceased) {
                alert("Deceased attribute is only applicable for WF");
                document.validateLogin.varid.focus();
                return false;
            }

            return true;

        }
        function set_focus() {
            document.validateLogin.userid.focus();
        }

        $(document).ready(function () {
            $("#varid").focusout(function () {
              if ($("#varid").val().trim() == 'VitalityUS' || $("#varid").val().trim() == 'VitalityCA' ){
                    if (!$("#authorization_code").length) {
                        var innerHtml = ' <div id="authorization_code"><label for="code">Code(Only for Vitality external program)</label>\n' +
                            '                <input id="code" name="code" type="text"></div>';
                        $(innerHtml).appendTo("#varid_row");
                    }else{
                        $("#authorization_code").remove();
                    }
                  if ($("#varid").val().trim() == 'VitalityUS' && $("#programid").val().trim() == 'TVGCorporate') {
                      if (!$("#pricingId_row").length) {
                          $.get("/apple-gr/service/priceModelsByVarProgram?var=" + $("#varid").val() + "&program=" + $("#programid").val(), function (data, status) {
                              data=[
                                  {
                                      "priceKey": "ZeroEmployerSubsidy",
                                      "priceType" :"Subsidy"
                                  },
                                  {
                                      "priceKey": "OneMonthEmployerSubsidy",
                                      "priceType" :"Subsidy"
                                  },
                                  {
                                      "priceKey": "TwoMonthEmployerSubsidy",
                                      "priceType" :"Subsidy"
                                  },
                                  {
                                      "priceKey": "ThreeMonthEmployerSubsidy",
                                      "priceType" :"Subsidy"
                                  },
                                  {
                                      "priceKey": "FourMonthEmployerSubsidy",
                                      "priceType" :"Subsidy"
                                  },
                                  {
                                      "priceKey": "FiveMonthEmployerSubsidy",
                                      "priceType" :"Subsidy"
                                  },
                                  {
                                      "priceKey": "SixMonthEmployerSubsidy",
                                      "priceType" :"Subsidy"
                                  },
                                  {
                                      "priceKey": "SevenMonthEmployerSubsidy",
                                      "priceType" :"Subsidy"
                                  },
                                  {
                                      "priceKey": "EightMonthEmployerSubsidy",
                                      "priceType" :"Subsidy"
                                  },
                                  {
                                      "priceKey": "NineMonthEmployerSubsidy",
                                      "priceType" :"Subsidy"
                                  },
                                  {
                                      "priceKey": "TenMonthEmployerSubsidy",
                                      "priceType" :"Subsidy"
                                  },
                                  {
                                      "priceKey": "TwelveMonthEmployerSubsidy",
                                      "priceType" :"Subsidy"
                                  },
                                  {
                                      "priceKey": "EighteenMonthEmployerSubsidy",
                                      "priceType" :"Subsidy"
                                  }

                              ];
                              var innerHtml = '<div id="pricingId_row"><label for="pricingIdSelect">Pricing Model Id:</label><select id="pricingIdSelect" name="pricingIdSelect">';
                              $.each(data, function (key, value) {
                                  if (value.priceType == 'Subsidy') {
                                      innerHtml += '<option value=' + value.priceKey + '>' + value.priceKey + '</option>';
                                  }
                              });
                              innerHtml += '</select></div>';
                              $(innerHtml).appendTo("#varid_row");
                          });
                      }else{
                          $("#pricingId_row").remove();
                      }
                  }
                }
                else {
                    if ($("#pricingId_row").length) {
                        $("#pricingId_row").remove();
                    }
                    if ($("#authorization_code").length) {
                        $("#authorization_code").remove();
                    }
                }

            });

        });


    </script>


<body onLoad="set_focus()">
<header id="header">
    <div class="header-wrap">
        <section class="header-container">
            <h1><img src="../images/B2SLogo.png" alt="Bridge2 Solutions"/></h1>

            <div class="platform-logo"><img src="../merchandise/apple-gr/assets/img/logo/AppleBlackExtraSmall.png"
                                            alt="Bridge2 Solutions"/></div>
            <div class="platform-title">
                <h2>B2S - Apple Platform
                    <span>Login Page</span></h2>
            </div>
        </section>
    </div>
</header>

<main id="body">
    <div class="body-container">
    <%
        if (request.getParameter("returnTest") != null) {
    %>
        <h2>[Coming from : <%=ESAPI.encoder().encodeForHTML(request.getParameter("returnTest"))%>]</h2>
            <%
        }
    %>
        <h3>Enter your login information</h3>

        <form name="validateLogin" id="validateLogin" action="/apple-gr/ValidateLoginAction.do" method="post">
            <div><label for="userid">User Id:</label>
                <input id="userid" name="userid" type="text"/></div>
            <div><label for="pword">Password:</label>
                <input id="pword" name="pword" type="password"/></div>
            <div><label for="programid">Program Code:</label>
                <input id="programid" name="programid" type="text"/></div>
            <div id="varid_row"><label for="varid">Client Code:</label>
                <input id="varid" name="varid" type="text"/></div>
            <div><label for="locale">Locale:</label>
                <input id="locale" name="locale" type="text"></div>
            <div><label for="discountcode">Discount Code:</label>
                <input id="discountcode" name="discountcode" type="text"></div>
            <div><label for="pricingTier">Pricing Tier:</label>
                <input id="pricingTier" name="pricingTier" type="text"></div>
            <div><label for="isEligibleForPayrollDeduction">
                <input id="isEligibleForPayrollDeduction" name="isEligibleForPayrollDeduction" type="checkbox" value="true" />
                <span>Eligible for payroll deduction: (Applicable only for payroll deduction enabled vars and programs)</span></label></div>
            <div><label for="isEligibleForDiscount">
                <input id="isEligibleForDiscount" name="isEligibleForDiscount" type="checkbox" value="true" />
                <span>Eligible for Discount: (Currently applicable only for B2S EPP)</span></label></div>
            <div><label for="isBrowseOnly">
                <input id="isBrowseOnly" name="isBrowseOnly" type="checkbox" value="true" />
                <span>Browse Only (Currently applicable only for WF)</span></label></div>
            <div><label for="isAnonymous">
                <input id="isAnonymous" name="isAnonymous" type="checkbox" value="true" />
                <span>Anonymous Only (Currently applicable only for FDR/FDR_PSCU/PNC/Delta/WF)</span></label></div>
            <div><label for="deceased">
                <input id="deceased" name="deceased" type="checkbox" value="true" />
                <span>Deceased (Currently applicable only for WF)</span></label></div>
            <div><label for="promotion">
                <input id="promotion" name="promotion" type="checkbox" value="true" />
                <span>Promotional Subscription (Currently applicable only for UA)</span></label></div>
            <div><label for="isAgentBrowse">
                <input id="isAgentBrowse" name="agentBrowse" type="checkbox" />
                <span>Agent Browse (Currently applicable only for FDR Call Agents)</span></label></div>
            <div class="submit-button">
                <input type=submit value="Sign In" class="btn-base blue-btn" onClick="return validate_data()"/>
            </div>
        </form>
    </div>
</main>

<footer id="footer">
    <div class="footer-container">
        <p>Please sign in using our secure servers</p>

        <p>Version: <b>${applicationScope['Implementation-Version']}</b> (Build time ${applicationScope['Build-Timestamp']})</p>

        <p>Build Number: <b>${applicationScope['B2S-Version']}</b></p>
    </div>
</footer>

</body>
</HTML>
