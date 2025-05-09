package com.b2s.apple.config;

import com.b2s.apple.spring.DataSourceTestConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;

import java.security.SecureRandom;

@RunWith(SpringRunner.class)
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class, classes = {ApplicationConfig.class,
        DataSourceTestConfiguration.class})
@WebAppConfiguration
public class ApplicationConfigTest {

    @Autowired
    ApplicationConfig applicationConfig;

    @Autowired
    BCryptPasswordEncoder bcryptEncoder;

    @Autowired
    SecureRandom secureRandom;

    @Test
    public void testPasswordEncryptWithDifferentLength()  {
        BCryptPasswordEncoder enc1 = new BCryptPasswordEncoder(13, secureRandom);
        BCryptPasswordEncoder enc2 = new BCryptPasswordEncoder(11, secureRandom);
        String str1 = enc1.encode("Password!");
        String str2 = enc2.encode("Password!");

        Assert.assertNotEquals(str1, str2);
    }

    @Test
    public void testPasswordDecryptWithDifferentLength()  {
        BCryptPasswordEncoder enc1 = new BCryptPasswordEncoder(13, secureRandom);
        BCryptPasswordEncoder enc2 = new BCryptPasswordEncoder(11, secureRandom);
        String str1 = enc1.encode("Password!");
        String str2 = enc2.encode("Password!");

        Assert.assertTrue(enc1.matches("Password!", str1));
        Assert.assertTrue(enc2.matches("Password!", str2));
    }
}
