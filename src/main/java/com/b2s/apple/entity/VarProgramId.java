package com.b2s.apple.entity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class VarProgramId implements Serializable {

    @Column(name = "var_id")
    private String varId;

    @Column(name = "program_id")
    private String programId;

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final VarProgramId that = (VarProgramId) o;
        return varId.equals(that.varId) &&
            programId.equals(that.programId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(varId, programId);
    }


}
