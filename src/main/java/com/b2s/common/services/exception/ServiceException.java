package com.b2s.common.services.exception;



/**
 *  <p>
 *  This exception is thrown while interfacing with external systems.
 @author sjonnalagadda
  * Date: 7/11/13
  * Time: 4:13 PM
 */
public class ServiceException extends Exception {

    private static final long serialVersionUID = -4028036110070787996L;

    private final int errorCode;
    private final ErrorSeverity errorSeverity;


    /**
     * Constructs exception from <code>ServiceExceptionEnums</code> enum constants
     *
     * @param serviceExceptionEnums exception details.
     */

    public  ServiceException(final ServiceExceptionEnums serviceExceptionEnums){
        super(serviceExceptionEnums.getErrorMessage());
        this.errorCode =  serviceExceptionEnums.getErrorCode();
        this.errorSeverity =  serviceExceptionEnums.getErrorSeverity();
    }
   /**
     * Constructs exception from <code>ServiceExceptionEnums</code> enum constants
     *
     * @param serviceExceptionEnums exception details.
     */

    public  ServiceException(final ServiceExceptionEnums serviceExceptionEnums, String[] args){
        super(serviceExceptionEnums.getErrorMessage(args));
        this.errorCode =  serviceExceptionEnums.getErrorCode();
        this.errorSeverity =  serviceExceptionEnums.getErrorSeverity();
    }

    /**
     * Constructs exception from <code>ServiceExceptionEnums</code> enum constants and cause of type
     * <code>Throwable</code>
     *
     * @param serviceExceptionEnums exception details.
     * @param th root cause of exception.
     */

    public  ServiceException(final ServiceExceptionEnums serviceExceptionEnums, final Throwable th){
        super(serviceExceptionEnums.getErrorMessage(), th);
        this.errorCode =  serviceExceptionEnums.getErrorCode();
        this.errorSeverity =  serviceExceptionEnums.getErrorSeverity();
    }

    /**
       @return int error code related to exception
     */

    public int getErrorCode() {
        return errorCode;
    }

    /**
     @return String severity of exception
     */

    public String getErrorSeverity(){
        return  errorSeverity.name();
    }
}
