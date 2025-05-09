package com.b2s.rewards.apple.model;

import javax.persistence.*;

/**
 * Created by rpillai on 2/22/2017.
 */
@Entity
@Table(name="var_program_message")
public class VarProgramMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, name = "var_id", columnDefinition = "VARCHAR(50)")
    private String varId;

    @Column(nullable = false, name = "program_id", columnDefinition = "VARCHAR(50)")
    private String programId;

    @Column(nullable = false, name = "code", columnDefinition = "VARCHAR(500)")
    private String code;

    @Column(nullable = false, name = "locale", columnDefinition = "VARCHAR(50)")
    private String locale;

    @Column(nullable = false, name = "message", columnDefinition = "NVARCHAR(MAX)")
    private String message;

    @Column(name = "code_type", columnDefinition = "VARCHAR(50)")
    private String codeType;


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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(final String codeType) {
        this.codeType = codeType;
    }
}
