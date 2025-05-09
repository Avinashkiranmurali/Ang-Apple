package com.b2s.rewards.apple.dao;

import com.b2s.rewards.apple.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/*** Created by srukmagathan on 8/23/2016.
 */
@Repository(value = "OtpDao")
@Transactional
public interface OtpDao extends JpaRepository<Otp,Long> {

    Otp findByEmailId(final String emailId);

    default Otp getByEmailId(final String emailId){
        return findByEmailId(emailId);
    }
}
