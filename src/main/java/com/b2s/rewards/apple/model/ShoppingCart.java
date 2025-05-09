package com.b2s.rewards.apple.model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by rperumal on 7/15/2015.
 */


/**
 * Created by rperumal on 9/9/2015.
 */

@Entity
@Table(name = "shopping_cart",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"var_id", "program_id", "user_id"})})
public class ShoppingCart {

    private Long id;
    private String varId;
    private String programId;
    private String userId;

    @Transient
    private List<ShoppingCartItem> shoppingCartItems;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, columnDefinition = "NUMERIC")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "var_id")
    public String getVarId() {
        return varId;
    }

    public void setVarId(String varId) {
        this.varId = varId;
    }

    @Column(name="program_id", nullable = false)
    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    @Column(name = "user_id", nullable = false)
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "shoppingCart", cascade=CascadeType.REMOVE)
    public List<ShoppingCartItem> getShoppingCartItems() {
        return shoppingCartItems;
    }

    public void setShoppingCartItems(List<ShoppingCartItem> shoppingCartItems) {
        this.shoppingCartItems = shoppingCartItems;
    }

}