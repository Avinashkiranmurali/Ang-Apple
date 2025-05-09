package com.b2s.apple.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class BasicAuthConfiguration extends WebSecurityConfigurerAdapter {

    private static final String ROLE_AEP = "ROLE_AEP";
    private static final String ROLE_VITALITYUS = "ROLE_VITALITYUS";
    private static final String ROLE_VITALITYCA = "ROLE_VITALITYCA";
    private static final String ROLE_BSWIFT = "ROLE_BSWIFT";
    private static final String ROLE_WEIGHTWATCHERS = "ROLE_WEIGHTWATCHERS";
    private static final String ROLE_AUS = "ROLE_AUS";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_GRASSROOTSUK = "ROLE_GRASSROOTSUK";

    @Value("${AEP_USER}")
    private String AEP_USER;
    @Value("${AEP_PWD}")
    private String AEP_PWD;
    @Value("${VITALITY_US_USER}")
    private String VITALITY_US_USER;
    @Value("${VITALITY_US_PWD}")
    private String VITALITY_US_PWD;
    @Value("${VITALITY_CA_USER}")
    private String VITALITY_CA_USER;
    @Value("${VITALITY_CA_PWD}")
    private String VITALITY_CA_PWD;
    @Value("${PPC_US_USER}")
    private String PPC_US_USER;
    @Value("${PPC_US_PWD}")
    private String PPC_US_PWD;
    @Value("${WEIGHTWATCHERS_US_USER}")
    private String WEIGHTWATCHERS_US_USER;
    @Value("${WEIGHTWATCHERS_US_PWD}")
    private String WEIGHTWATCHERS_US_PWD;
    @Value("${AUS_USER}")
    private String AUS_USER;
    @Value("${AUS_PWD}")
    private String AUS_PWD;
    @Value("${ADMIN_USER}")
    private String ADMIN_USER;
    @Value("${ADMIN_PWD}")
    private String ADMIN_PWD;
    @Value("${GRASSROOTSUK_USER}")
    private String GRASSROOTSUK_USER;
    @Value("${GRASSROOTSUK_PWD}")
    private String GRASSROOTSUK_PWD;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers()
            .contentSecurityPolicy("frame-ancestors 'none'");
        http.httpBasic().and().authorizeRequests()
                .antMatchers("/service/order/confirmPurchase/**").hasAnyAuthority(ROLE_VITALITYUS,ROLE_WEIGHTWATCHERS,ROLE_VITALITYCA,ROLE_GRASSROOTSUK)
                .antMatchers("/service/order/status/**").hasAnyAuthority(ROLE_BSWIFT)
                .antMatchers("/service/merchants/create").hasAnyAuthority(ROLE_AEP)
                .antMatchers("/services/{varId}/{programId}/{userId}/cart/count").permitAll()
                .antMatchers("/service/notification/sendEmail").hasAnyAuthority(ROLE_AUS,ROLE_ADMIN)
                .antMatchers("/service/orders/**").hasAnyAuthority(ROLE_VITALITYUS,ROLE_WEIGHTWATCHERS,ROLE_VITALITYCA,ROLE_GRASSROOTSUK)
                .and().httpBasic().authenticationEntryPoint(new B2SBasicAuthenticationEntryPoint())
                .and().csrf().disable();

        http.exceptionHandling().accessDeniedHandler(new B2SAccessDeniedHandler());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
            .withUser(AEP_USER).password(AEP_PWD).authorities(ROLE_AEP)
            .and()
            .withUser(VITALITY_US_USER).password(VITALITY_US_PWD).authorities(ROLE_VITALITYUS)
            .and()
            .withUser(VITALITY_CA_USER).password(VITALITY_CA_PWD).authorities(ROLE_VITALITYCA)
            .and()
            .withUser(PPC_US_USER).password(PPC_US_PWD).authorities(ROLE_BSWIFT)
            .and()
            .withUser(WEIGHTWATCHERS_US_USER).password(WEIGHTWATCHERS_US_PWD).authorities(ROLE_WEIGHTWATCHERS)
            .and()
            .withUser(AUS_USER).password(AUS_PWD).authorities(ROLE_AUS)
            .and()
            .withUser(ADMIN_USER).password(ADMIN_PWD).authorities(ROLE_ADMIN)
            .and()
            .withUser(GRASSROOTSUK_USER).password(GRASSROOTSUK_PWD).authorities(ROLE_GRASSROOTSUK);
    }
}
