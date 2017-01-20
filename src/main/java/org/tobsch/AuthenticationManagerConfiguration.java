package org.tobsch;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.ldap.core.support.LdapContextSource;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;


@Configuration
@EnableWebSecurity
public class AuthenticationManagerConfiguration extends GlobalAuthenticationConfigurerAdapter {

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {

        auth.ldapAuthentication()
            .userSearchBase("ou=People")
            .userSearchFilter("cn={0}")
            .groupSearchBase("ou=Groups")
            .groupSearchFilter("member={0}")
            .contextSource(contextSource());
    }


    @Bean
    public LdapContextSource contextSource() {

        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl("ldap://ldap.example.org:389");
        contextSource.setBase("dc=example,dc=org");
        // contextSource.setAuthenticationStrategy(new DefaultTlsDirContextAuthenticationStrategy());

        // This implementation with reconnect works
        contextSource.setAuthenticationStrategy(new FixDefaultTlsDirContextAuthenticationStrategy());

        return contextSource;
    }
}
