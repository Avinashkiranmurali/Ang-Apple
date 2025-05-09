package com.b2s.apple.entity;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "var_program")
public class VarProgramEntity {

    @Id
    @EmbeddedId
    private VarProgramId varProgramId;

}
