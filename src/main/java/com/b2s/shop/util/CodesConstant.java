package com.b2s.shop.util;

public class CodesConstant {

    public static String CodeActive = "0";

    public static String codeRedeemed = "1";

    public static String codeDisabled = "2";
    public static String codeAttached = "3";
    
    public static int programPoints = 0;

    public static int programInternational = 3;

    /**
     * The only programs that have the VAR_PROGRAM.CATALOG_WIDTH = '4' is varId='Consumer' and programId in
     * ('MRZEYMERCH','MRZEYTIX','MRZEYTRAVEL'). These programs should be removed when dealing with HTML 5 conversion.
     */
    @Deprecated
    public static int programConsumer = 4;

}
