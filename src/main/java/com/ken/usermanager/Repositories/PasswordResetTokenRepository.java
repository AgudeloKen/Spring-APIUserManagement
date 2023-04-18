package com.ken.usermanager.Repositories;

import com.ken.usermanager.Domains.PasswordResetToken;
import com.ken.usermanager.Domains.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);

    PasswordResetToken findByUser(User user);

    Boolean existsByUser(User user);

}
