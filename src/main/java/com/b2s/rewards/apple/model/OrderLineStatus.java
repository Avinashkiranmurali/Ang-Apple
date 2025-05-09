package com.b2s.rewards.apple.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by vmurugesan on 10/14/2016.
 */
@Entity
@Table(name="order_status")
public class OrderLineStatus implements Serializable {

    private static final long serialVersionUID = -3655299137643706464L;

    @Id
    @Column(name= "status_id")
    private Integer statusId;

    @Column(name= "[desc]")
    private  String statusDesc;

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(final String statusDesc) {
        this.statusDesc = statusDesc;
    }
}
