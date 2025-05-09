package com.b2s.security.saml;

import com.b2s.apple.spring.SamlRewardStepTestConfiguration;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes =
    {SamlRewardStepTestConfiguration.class})
@WebAppConfiguration
public class SAMLVelocityEngineTest {

    @Autowired
    private VelocityEngine samlVelocityEngine;

    @Test
    public void testVelocityEngineInitOK() {

        final StringWriter writer = new StringWriter();
        final String expectedResult =
            "<!DOCTYPE html><html><body><form id=\"postSamlForm\" method=\"post\" action=\"${action}\"></form><script>window.onload = function() "
                + "{document.getElementById(\"postSamlForm\").submit();};</script></body></html>";

        final Map<String, String> contextMap = new HashMap<>();
        contextMap.put("", "");
        try {
            samlVelocityEngine.getTemplate("post-saml-response.vm").merge(new VelocityContext(contextMap), writer);
        } catch (final Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(expectedResult.replaceAll("\\s+", ""), writer.toString().replaceAll("\\s+", ""));
    }

}
