package com.b2s.rewards.apple.model;

import com.b2s.util.Assert;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Represents the order_line_status_history table.
 */
@Entity
@Table(name = "order_line_status_history")
public class OrderLineStatusHistory implements Serializable {

    private static final long serialVersionUID = 6562656657507510110L;

    @EmbeddedId
    private OrderLineStatusHistoryId id;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final OrderLineStatusHistory that = (OrderLineStatusHistory) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id.toString();   // In this case, the id and entity are the same.
    }

    public OrderLineStatusHistoryId getId() {
        return id;
    }

    public void setId(final OrderLineStatusHistoryId id) {
        Assert.notNull(id, "id in OrderLineStatusHistory must not be null.");
        this.id = id;
    }

}
