package com.b2s.apple.entity;

import javax.persistence.*;
/**
 * Created by skither on 12/5/2018.
 */
@Entity
@Table(name="merchant")
public class MerchantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "merchant_id")
    private  Integer merchantId;
    @Column(name = "supplier_id")
    private  Integer supplierId;
    @Column(name = "name")
    private  String  name;
    @Column(name = "simple_name")
    private  String  simpleName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Integer merchantId) {
        this.merchantId = merchantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }
}
