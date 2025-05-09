package com.b2s.rewards.apple.runner;

import com.microsoft.sqlserver.jdbc.SQLServerConnectionPoolDataSource;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Created by ssrinivasan on 6/1/2015.
 *
 * Custom Junit4 Class runner, Start all required mock servers for Apple Unit Testcases
 *
 */
public class AppleJunit4ClassRunner extends SpringJUnit4ClassRunner {

    public AppleJunit4ClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected Statement withBeforeClasses(final Statement statement) {
            try {
                // Create initial context
                System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.naming.java.javaURLContextFactory");
                System.setProperty(Context.URL_PKG_PREFIXES,
                    "org.apache.naming");
                InitialContext ic = new InitialContext();

                ic.createSubcontext("java:");
                ic.createSubcontext("java:comp");
                ic.createSubcontext("java:comp/env");
                ic.createSubcontext("java:comp/env/jdbc");

                // Construct DataSource
                SQLServerConnectionPoolDataSource ds = new SQLServerConnectionPoolDataSource();
                ds.setURL("jdbc:sqlserver://172.40.8.82:1433;databaseName=APPLE_CORE;;user=APPLE_CORE;password=N!RDyKc37&APPLE");
                ic.bind("java:comp/env/jdbc/b2r", ds);
            } catch (NamingException ex) {
                LoggerFactory.getLogger(SpringJUnit4ClassRunner.class).error(null, ex);
            }
        return super.withBeforeClasses(statement);
    }

    @Override
    protected Statement withAfterClasses(final Statement statement) {
        return super.withAfterClasses(statement);
    }
}
