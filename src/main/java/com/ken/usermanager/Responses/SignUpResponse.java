package com.ken.usermanager.Responses;

import com.ken.usermanager.Domains.User;

public class SignUpResponse {

    private String email;

    private String phone;

    public SignUpResponse(User user){
        this.email = user.getEmail();
        this.phone = user.getPhone();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
