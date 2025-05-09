package com.b2s.common.services.transformers;

import com.b2s.rewards.apple.model.Program;

/**
 * <p>
 * Used by transformers that transform Request / Response between services layer and core model.
 @author sjonnalagadda
  * Date: 7/11/13
  * Time: 4:13 PM
 *
 */
public interface Transformer<FROM,TO> {

      TO transform(FROM from, Helper helper, Program program);

}
