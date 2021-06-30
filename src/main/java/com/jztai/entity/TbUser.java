package com.jztai.entity;

public class TbUser {

    private String userName;

    private Integer age;

    private String address;

    public TbUser(String userName, Integer age, String address) {
        this.userName = userName;
        this.age = age;
        this.address = address;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
