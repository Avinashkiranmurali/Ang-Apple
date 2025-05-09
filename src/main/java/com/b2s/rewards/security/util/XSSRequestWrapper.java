package com.b2s.rewards.security.util;

import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.ESAPI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.regex.Pattern;

public class XSSRequestWrapper extends HttpServletRequestWrapper {

    // Avoid null characters
    private static final Pattern PATTERN_NULL = Pattern.compile("\0");

    private static final Pattern[] PATTERNS = new Pattern[]{
        // Avoid anything between script tags
        Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
        // Avoid anything in a src='...' type of expression
        Pattern.compile(
            "src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile(
            "src[\r\n]*=[\r\n]*\\\"(.*?)\\\"",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Remove any lonesome </script> tag
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        //Remove any lonesome <script ...> tag
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid eval(...) expressions
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid expression(...) expressions
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid javascript:... expressions
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        // Avoid vbscript:... expressions
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        // Avoid alert... expressions
        Pattern.compile("alert(.*?)", Pattern.CASE_INSENSITIVE),
        // Avoid prompt... expressions
        Pattern.compile("prompt(.*?)", Pattern.CASE_INSENSITIVE),
        // Avoid onload= expressions
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid \" and "
        Pattern.compile("\\\\\"|\"", Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid \'
        Pattern.compile("\\\\\'", Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid special characters like ), ;, <, >
        Pattern.compile("[);<>]", Pattern.MULTILINE | Pattern.DOTALL)
    };

    public XSSRequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
    }

    public String[] getParameterValues(String parameter) {

        String[] values = super.getParameterValues(parameter);
        if (values == null) {
            return null;
        }
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = cleanXSS(values[i]);
        }
        return encodedValues;
    }

    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        if (value == null) {
            return null;
        }
        return cleanXSS(value);
    }

    public String getRequestURI() {
        String value = super.getRequestURI();
        if (value == null) {
            return null;
        }
        return cleanXSS(value);
    }

    public String getHeader(String name) {
        String value = super.getHeader(name);
        if (value == null) {
            return null;
        }
        return cleanXSS(value);

    }

    public String getRemoteAddr() {
        String value = super.getRemoteAddr();
        if (value == null) {
            return null;
        }
        return cleanXSS(value);
    }

    public String getQueryString() {
        String value = super.getQueryString();
        if (value == null) {
            return null;
        }
        return cleanXSS(value);
    }

    public static String cleanXSS(String value) {
        if (StringUtils.isNotEmpty(value)) {

            String stripedValue = getCanonicalizedString(value);

            // Remove all sections that match a pattern
            for (final Pattern scriptPattern : PATTERNS) {
                stripedValue = scriptPattern.matcher(stripedValue).replaceAll("");
            }

            return stripedValue;
        }

        return value;
    }

    public static String getCanonicalizedString(final String value){
        String stripedValue = ESAPI.encoder().canonicalize(value);

        // Avoid null characters
        stripedValue = PATTERN_NULL.matcher(stripedValue).replaceAll("");
        return stripedValue;
    }

}