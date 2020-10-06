package com.example.diushuttle;

public class userInfoClass {
    private String firstName, lastName, email,  phone, id;
    public userInfoClass(){};
    public userInfoClass(String firstName, String lastName, String email, String phone  ){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }
    public String getFname() {
        return firstName;
    }

    public String getElname() {
        return lastName;
    }

    public String getMail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
    public void setUserId(String id){
        this.id = id;
    }
    public String getUserId() {
        return id;
    }


}
