package com.b2s.apple.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Table(name = "var_program_credit_adds_filter")
@Entity
public class VarProgramCreditAddsFilterEntity {

    @EmbeddedId
    private VarProgramFilterId varProgramFilterId;

    @Column(name = "lastupdate_user")
    private String lastupdateUser;
    @Column(name = "lastupdate_time")
    private Date lastupdateTime;

    public VarProgramFilterId getVarProgramFilterId() {
        return varProgramFilterId;
    }

    public void setVarProgramFilterId(final VarProgramFilterId varProgramFilterId) {
        this.varProgramFilterId = varProgramFilterId;
    }

    public String getLastupdateUser() {
        return lastupdateUser;
    }

    public void setLastupdateUser(final String lastupdateUser) {
        this.lastupdateUser = lastupdateUser;
    }

    public Date getLastupdateTime() {
        return lastupdateTime;
    }

    public void setLastupdateTime(final Date lastupdateTime) {
        this.lastupdateTime = lastupdateTime;
    }

    @Embeddable
    public static class VarProgramFilterId implements Serializable {

        @Column(name = "var_id")
        private String varId;

        @Column(name = "program_id")
        private String programId;

        @Column(name = "filter")
        private String filter;

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

        public String getFilter() {
            return filter;
        }

        public void setFilter(final String filter) {
            this.filter = filter;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final VarProgramFilterId that = (VarProgramFilterId) o;
            return Objects.equals(varId, that.varId) &&
                Objects.equals(programId, that.programId) &&
                Objects.equals(filter, that.filter);
        }

        @Override
        public int hashCode() {
            return Objects.hash(varId, programId, filter);
        }
    }

}