package com.ken.usermanager.DTO;

import com.ken.usermanager.Domains.UserImage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserImageDTO {

    private Long id;

    private String imageURL;


    public UserImageDTO(UserImage userImage){
        this.id = userImage.getId();
        this.imageURL = userImage.getImageURL();
    }
}
