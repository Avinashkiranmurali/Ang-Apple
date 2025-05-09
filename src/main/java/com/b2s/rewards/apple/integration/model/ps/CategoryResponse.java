package com.b2s.rewards.apple.integration.model.ps;

import com.b2s.service.product.client.domain.CategoryNode;

import java.util.List;

/**
 * Created by rpillai on 11/18/2016.
 */
public class CategoryResponse {

    private List<CategoryNode> response;

    public List<CategoryNode> getResponse() {
        return response;
    }

    public void setResponse(List<CategoryNode> response) {
        this.response = response;
    }
}
