package com.ken.usermanager.DTO;

import com.ken.usermanager.Domains.User;
import com.ken.usermanager.Domains.UserImage;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class UserDTO implements Serializable {

    private Long id;

    private String email;

    private String phone;

    private Boolean enabled;

    private String authority;

    private LocalDate emailVerifiedAt;

    private UserImageDTO image;

    private Date createdAt;

    public UserDTO(User user){
        this.id = user.getId();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.enabled = user.isEnabled();
        this.authority = user.getAuthority().getName();
        this.emailVerifiedAt = user.getEmailVerifiedAt();
        this.createdAt = user.getCreatedAt();
        this.image = new UserImageDTO(user.getUserImage());
    }
}
