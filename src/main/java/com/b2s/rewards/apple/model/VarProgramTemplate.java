package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.math.BigInteger;

/**
 * @author rkumar 2020-02-10
 */
@Entity
@Table(name = "var_program_template")
public class VarProgramTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    @Column(name = "var_id")
    private String varId;

    @Column(name = "program_id")
    private String programId;

    @Column(name = "config_data")
    private String configData;

    @Column(name = "is_active")
    private Boolean isActive;

    public BigInteger getId() {
        return id;
    }

    public void setId(final BigInteger id) {
        this.id = id;
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

    public String getConfigData() {
        return configData;
    }

    public void setConfigData(final String configData) {
        this.configData = configData;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(final Boolean active) {
        isActive = active;
    }
}
