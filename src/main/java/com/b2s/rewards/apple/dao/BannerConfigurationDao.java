package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.BannerConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.b2s.rewards.common.util.CommonConstants.DEFAULT_VAR_PROGRAM;


/**
 * Created by rperumal on 12/14/2015
 */
@Repository("bannerConfigurationDao")
@Transactional
public interface BannerConfigurationDao extends JpaRepository<BannerConfiguration, Long> {

    List<BannerConfiguration> findByName(String name);
    default List<BannerConfiguration> getByName(String name) {
        return findByName(name);
    }


}
