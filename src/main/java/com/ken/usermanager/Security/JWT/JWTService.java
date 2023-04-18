package com.ken.usermanager.Security.JWT;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ken.usermanager.Domains.User;
import com.ken.usermanager.Exceptions.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class JWTService {

    @Value("${api.jwt.secret}")
    private String secret;

    public String generateToken(User user) throws CustomException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("Ken")
                    .withSubject(user.getEmail())
                    .withClaim("id", user.getId())
                    .withExpiresAt(expirationTime())
                    .sign(algorithm);
        }catch (JWTCreationException exception){
            throw new CustomException("The token could not be created.");
        }
    }

    public Instant expirationTime(){
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-05:00"));
    }

    public String getSubject(String token) throws CustomException {
        if(token == null){
            throw new CustomException("The token cannot be null.");
        }
        DecodedJWT decodedJWT = null;
        try{
            Algorithm algorithm = Algorithm.HMAC256(secret);
            decodedJWT = JWT.require(algorithm)
                    .withIssuer("Ken")
                    .build()
                    .verify(token);
        }catch (JWTVerificationException exception){
            throw new CustomException("The token is not valid.");
        }

        assert decodedJWT != null;
        return decodedJWT.getSubject();
    }

}
