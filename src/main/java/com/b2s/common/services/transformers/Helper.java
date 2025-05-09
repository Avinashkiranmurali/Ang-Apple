package com.b2s.common.services.transformers;

import java.util.Locale;
import java.util.Optional;

/**
 * <p>
 * Interface for any transformers objects that additional data from request, for transforming response
 * to core mode.
 @author sjonnalagadda
  * Date: 8/06/13
  * Time: 2:49 PM
 *
 */
public interface Helper {
    Optional<Locale> getUserLanguage();
}
