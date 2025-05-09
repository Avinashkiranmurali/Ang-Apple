package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.util.Date;

/*** Created by srukmagathan on 8/23/2016.
 */
@Entity
@Table(name = "otp")
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_id")
    private String emailId;

    private String otp;

    @Column(name = "created_date")
    private Date createdDate;

    @Column(name = "used")
    private String isUsed;

    @Column(name = "usage_date")
    private Date usageDate;


    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(final Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(final String emailId) {
        this.emailId = emailId;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(final String isUsed) {
        this.isUsed = isUsed;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(final String otp) {
        this.otp = otp;
    }

    public Date getUsageDate() {
        return usageDate;
    }

    public void setUsageDate(final Date usageDate) {
        this.usageDate = usageDate;
    }
}
