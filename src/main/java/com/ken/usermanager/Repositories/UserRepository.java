package com.ken.usermanager.Repositories;

import com.ken.usermanager.Domains.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    UserDetails findUserByEmail(String email);

    Boolean existsByEmail(String email);

}
