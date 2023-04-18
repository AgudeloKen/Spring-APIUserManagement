package com.ken.usermanager.Repositories;

import com.ken.usermanager.Domains.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {
}
