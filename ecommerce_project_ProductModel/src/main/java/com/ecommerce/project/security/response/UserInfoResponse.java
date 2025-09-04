package com.ecommerce.project.security.response;

import java.util.List;

public class UserInfoResponse {

    private Long id;
    private String jwtToken;
    private String userName;
    private List<String>roles;


    public UserInfoResponse(Long id,String jwtToken, String userName, List<String> roles) {
        this.id=id;
        this.jwtToken = jwtToken;
        this.userName = userName;
        this.roles = roles;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
