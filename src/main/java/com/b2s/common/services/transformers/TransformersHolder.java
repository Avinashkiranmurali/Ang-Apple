package com.b2s.common.services.transformers;

/**
 * <p>
 * Used to hold data a pair of transformers for converting request to service layer request and response from service layer
 * to core model.
 @author sjonnalagadda
  * Date: 08/07/13
  * Time: 12:07 PM
 *
 */
public class TransformersHolder<A,B,C,D> {

    private final Transformer<A,B> requestTransformer;
    private final Transformer<C,D> responseTransformer;

    public  TransformersHolder(final Transformer<A,B> requestTransformer, final Transformer<C,D> responseTransformer){
        this.requestTransformer =  requestTransformer;
        this.responseTransformer = responseTransformer;
    }

    public  Transformer<A,B> getRequestTransformer(){
        return requestTransformer;
    }
    public Transformer<C,D> getResponseTransformer(){
        return  responseTransformer;
    }
}
