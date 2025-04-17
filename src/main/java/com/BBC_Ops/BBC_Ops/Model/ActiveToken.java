package com.BBC_Ops.BBC_Ops.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ActiveToken {

    @Id
    private String email;

    @Column(length = 1000)
    private String token;

    // constructor, getters, setters

    @Override
    public String toString() {
        return "ActiveToken{" +
                "email='" + email + '\'' +
                ", token='" + token + '\'' +
                '}';
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


//    public ActiveToken(String email, String token) {
//        this.email = email;
//        this.token = token;
//    }


}
