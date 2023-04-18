package com.ken.usermanager.Exceptions;

public class JWTFailException extends Exception{

    public JWTFailException(String message){
        super(message);
    }
}
