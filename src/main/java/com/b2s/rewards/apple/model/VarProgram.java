package com.b2s.rewards.apple.model;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Created by rpillai on 6/24/2016.
 */
@Entity
@Table(name = "var_program")
public class VarProgram  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "var_program_KEY")
    private Integer varProgramKEY;

    @Column(name = "varid")
    private String varId;

    @Column(name = "programid")
    private String programId;

    @Column(name = "name")
    private String name;

    @Column(name = "active_ind")
    private String active;

    @Column(name = "imageurl")
    private String imageUrl;

    @Column(name = "conv_rate")
    private Double convRate;

    @Column(name = "point_name")
    private String pointName;

    @Column(name = "point_format")
    private String pointFormat;

    @Column(name = "demo_ind")
    private String demo;

    @Column(name = "catagory_title_color")
    private String remote;

    @Column(name = "faqs")
    private String faqs;

    @Column(name = "is_ack_terms_cond")
    private String enableAcknowledgeTermsConds;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumns({
        @JoinColumn(name = "var_id",  referencedColumnName = "varid", insertable = false, updatable = false),
        @JoinColumn(name = "program_id",  referencedColumnName = "programid",insertable = false, updatable = false),
    })
    private List<RedemptionOption> redemptionOptions;

    @OneToMany(fetch = FetchType.LAZY)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinColumns({
            @JoinColumn(name = "var_id", referencedColumnName = "varid", insertable = false, updatable = false),
            @JoinColumn(name = "program_id", referencedColumnName = "programid", insertable = false, updatable = false)
    })
    private List<VarProgramFinanceOption> varProgramFinanceOptions;

    public Integer getVarProgramKEY() {
        return varProgramKEY;
    }

    public void setVarProgramKEY(Integer varProgramKEY) {
        this.varProgramKEY = varProgramKEY;
    }

    public String getVarId() {
        return varId;
    }

    public void setVarId(String varId) {
        this.varId = varId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getConvRate() {
        return convRate;
    }

    public void setConvRate(Double convRate) {
        this.convRate = convRate;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getPointFormat() {
        return pointFormat;
    }

    public void setPointFormat(String pointFormat) {
        this.pointFormat = pointFormat;
    }

    public String getDemo() {
        return demo;
    }

    public void setDemo(String demo) {
        this.demo = demo;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String getFaqs() {
        return faqs;
    }

    public void setFaqs(String faqs) {
        this.faqs = faqs;
    }

    public List<RedemptionOption> getRedemptionOptions() {
        return redemptionOptions;
    }

    public void setRedemptionOptions(final List<RedemptionOption> redemptionOptions) {
        this.redemptionOptions = redemptionOptions;
    }

    public List<VarProgramFinanceOption> getVarProgramFinanceOptions() {
        return varProgramFinanceOptions;
    }

    public void setVarProgramFinanceOptions(List<VarProgramFinanceOption> varProgramFinanceOptions) {
        this.varProgramFinanceOptions = varProgramFinanceOptions;
    }

    public String getEnableAcknowledgeTermsConds() {

        return enableAcknowledgeTermsConds;
    }

    public void setEnableAcknowledgeTermsConds(final String enableAcknowledgeTermsConds) {

        this.enableAcknowledgeTermsConds = enableAcknowledgeTermsConds;
    }
}
