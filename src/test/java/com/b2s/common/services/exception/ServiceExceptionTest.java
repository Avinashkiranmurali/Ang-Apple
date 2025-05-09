package com.b2s.common.services.exception;


import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;


/**
 *  This is unit test class for <code>com.b2s.common.services.exception.ServiceException</code>
  @author sjonnalagadda
  * Date: 7/11/13
  * Time: 4:13 PM

 */
@RunWith(JUnit4.class)
public class ServiceExceptionTest {

    @Test
    public void testMemberVariablePopulation(){
        final ServiceException serviceException = new ServiceException(ServiceExceptionEnums.SERVICE_INITIALIZATION_EXCEPTION);
        assertSame("Error code is not matching", ServiceExceptionEnums.SERVICE_INITIALIZATION_EXCEPTION.getErrorCode(),
                serviceException.getErrorCode());
        assertSame("Error Severity is not matching", ServiceExceptionEnums.SERVICE_INITIALIZATION_EXCEPTION.getErrorSeverity().name(),
                serviceException.getErrorSeverity());
        assertSame("Error message is not matching", ServiceExceptionEnums.SERVICE_INITIALIZATION_EXCEPTION.getErrorMessage(),
                serviceException.getMessage());
    }

    @SuppressWarnings({"ThrowableInstanceNeverThrown", "UnusedAssignment"})
    @Test
    public void testNullMemberVariablePopulation(){
        //noinspection ProhibitedExceptionCaught
        try{
            final ServiceException serviceException = new ServiceException(null);
            throw new AssertionError("Exception should not reach this line");
        }catch(NullPointerException npe){
            assertThat( npe,  new IsInstanceOf(NullPointerException.class));
        }

    }

    @Test
    public void testActualEExceptionIsSetAsThrowable(){
        final Exception arrayIndexOutOfBound = new ArrayIndexOutOfBoundsException();
        final ServiceException serviceException = new ServiceException(ServiceExceptionEnums.SERVICE_INITIALIZATION_EXCEPTION,arrayIndexOutOfBound);
        assertSame("Error code is not matching", ServiceExceptionEnums.SERVICE_INITIALIZATION_EXCEPTION.getErrorCode(),
                serviceException.getErrorCode());
        assertSame("Error Severity is not matching", ServiceExceptionEnums.SERVICE_INITIALIZATION_EXCEPTION.getErrorSeverity().name(),
                serviceException.getErrorSeverity());
        assertSame("Error message is not matching", ServiceExceptionEnums.SERVICE_INITIALIZATION_EXCEPTION.getErrorMessage(),
                serviceException.getMessage());
        assertThat(serviceException.getCause(), new IsInstanceOf(ArrayIndexOutOfBoundsException.class));
   }

}
