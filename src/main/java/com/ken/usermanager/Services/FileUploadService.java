package com.ken.usermanager.Services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.ken.usermanager.Domains.User;
import com.ken.usermanager.Domains.UserImage;
import com.ken.usermanager.Repositories.UserImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class FileUploadService {

    private final Cloudinary cloudinary;
    private final UserImageRepository userImageRepository;

    public FileUploadService(Cloudinary cloudinary, UserImageRepository userImageRepository){
        this.cloudinary = cloudinary;
        this.userImageRepository = userImageRepository;
    }

    public UserImage fileUpload(MultipartFile image, User user) throws IOException {
        String publicId = UUID.randomUUID().toString();
        try {
           cloudinary.uploader().upload(image.getBytes(), Map.of("public_id", publicId));
           return saveUserImage(publicId, user);
        } catch (IOException exception) {
            throw new RuntimeException("Error uploading image.");
        }
    }

    private UserImage saveUserImage(String publicId, User user){
        String url = cloudinary.url().transformation(new Transformation().width(400).height(400).crop("fill")).generate(publicId);

        UserImage userImage = new UserImage();

        userImage.setUser(user);
        userImage.setImageURL(url);
        userImage.setPublicId(publicId);

        return userImageRepository.save(userImage);
    }
}
