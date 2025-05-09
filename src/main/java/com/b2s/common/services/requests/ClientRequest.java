package com.b2s.common.services.requests;

import java.util.Locale;
import java.util.Optional;

/**
 * <p>
 * Used for any request objects that need to interact with service layer.
 @author sjonnalagadda
  * Date: 7/11/13
  * Time: 4:13 PM
 *
 */

public interface ClientRequest {
    Optional<Locale> getUserLanguage();
}
