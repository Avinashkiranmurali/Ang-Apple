package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by rperumal on 9/9/2015.
 */

@Repository("appleShoppingCartDao")
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public interface ShoppingCartDao extends JpaRepository<ShoppingCart, Long> {

    @Query(value="select * from shopping_cart WITH (NOLOCK) where var_id=?1 and program_id=?2 and user_id=?3", nativeQuery = true)
    ShoppingCart get(String varId, String programId, String userId);

}

