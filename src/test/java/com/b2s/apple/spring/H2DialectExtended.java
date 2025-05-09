package com.b2s.apple.spring;

import org.hibernate.dialect.H2Dialect;

/**
 * Values of types "BOOLEAN" and "BIT" / "INTEGER"  are not comparable in H2 Version 2.0 + .
 * overriding the Hibenate's Dialect - toBooleanValueString method by which Hibernate will write :
 *     "WHERE myBooleanColumn = TRUE"
 *     instead of    "WHERE myBooleanColumn =  1"
 */
public class H2DialectExtended extends H2Dialect {
    @Override
    public String toBooleanValueString(final boolean bool) {
        return bool ? "TRUE" : "FALSE";
    }
}
