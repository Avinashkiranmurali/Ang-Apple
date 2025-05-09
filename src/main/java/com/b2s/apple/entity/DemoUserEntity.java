package com.b2s.apple.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author rjesuraj Date : 8/20/2019 Time : 12:41 PM
 */

@Entity
@Table(name = "demo_user")
public class DemoUserEntity {
    @EmbeddedId
    private DemoUserId demoUserId;

    @Column(name = "password")
    private String password;
    @Column(name = "activeind")
    private String activeind;
    @Column(name = "points")
    private Integer points;
    @Column(name = "firstname")
    private String firstname;
    @Column(name = "lastname")
    private String lastname;
    @Column(name = "addr1")
    private String addr1;
    @Column(name = "addr2")
    private String addr2;
    @Column(name = "city")
    private String city;
    @Column(name = "state")
    private String state;
    @Column(name = "zip")
    private String zip;
    @Column(name = "country")
    private String country;
    @Column(name = "phone")
    private String phone;
    @Column(name = "email")
    private String email;

    public DemoUserId getDemoUserId() {
        return demoUserId;
    }

    public void setDemoUserId(final DemoUserId demoUserId) {
        this.demoUserId = demoUserId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getActiveind() {
        return activeind;
    }

    public void setActiveind(final String activeind) {
        this.activeind = activeind;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(final Integer points) {
        this.points = points;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(final String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(final String lastname) {
        this.lastname = lastname;
    }

    public String getAddr1() {
        return addr1;
    }

    public void setAddr1(final String addr1) {
        this.addr1 = addr1;
    }

    public String getAddr2() {
        return addr2;
    }

    public void setAddr2(final String addr2) {
        this.addr2 = addr2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(final String zip) {
        this.zip = zip;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public static class DemoUserId implements Serializable {
        @Column(name = "programid")
        private String programId;
        @Column(name = "userid")
        private String userId;
        @Column(name = "varid")
        private String varId;

        public String getUserId() {
            return userId;
        }

        public void setUserId(final String userId) {
            this.userId = userId;
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
    }

}
