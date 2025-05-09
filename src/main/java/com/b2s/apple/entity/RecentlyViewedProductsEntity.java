package com.b2s.apple.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "recently_viewed_products")
public class RecentlyViewedProductsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "var_id")
    private String varId;
    @Column(name = "program_id")
    private String programId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "product_id")
    private String productId;
    @Column(name = "viewed_timestamp")
    private Date viewedDateTime;

    public String getVarId() {
        return varId;
    }

    public void setVarId(final String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(final String programId) {
        this.programId = programId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(final String productId) {
        this.productId = productId;
    }

    public Date getViewedDateTime() {
        return viewedDateTime;
    }

    public void setViewedDateTime(final Date viewedDateTime) {
        this.viewedDateTime = viewedDateTime;
    }
}
