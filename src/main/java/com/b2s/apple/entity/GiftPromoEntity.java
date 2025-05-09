package com.b2s.apple.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "var_program_gift_promo")
public class GiftPromoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "locale")
    private String locale;
    @Column(name = "var_id")
    private String varId;
    @Column(name = "program_id")
    private String programId;
    @Column(name = "qualifying_psid")
    private String qualifyingPsid;
    @Column(name = "gift_item_psid")
    private String giftItemPsid;
    @Column(name = "start_date")
    private Date startDate;
    @Column(name = "end_date")
    private Date endDate;
    @Column(name = "active")
    private boolean active;
    @Column(name = "discount", nullable = false)
    private Double discount;
    @Column(name = "discount_type", nullable = false)
    private String discountType;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

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

    public String getQualifyingPsid() {
        return qualifyingPsid;
    }

    public void setQualifyingPsid(final String qualifyingPsid) {
        this.qualifyingPsid = qualifyingPsid;
    }

    public String getGiftItemPsid() {
        return giftItemPsid;
    }

    public void setGiftItemPsid(final String giftItemPsid) {
        this.giftItemPsid = giftItemPsid;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(final Double discount) {
        this.discount = discount;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(final String discountType) {
        this.discountType = discountType;
    }
}