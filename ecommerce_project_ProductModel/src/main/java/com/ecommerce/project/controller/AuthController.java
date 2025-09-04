package com.ecommerce.project.controller;


import com.ecommerce.project.security.jwt.JWTUtils;
import com.ecommerce.project.security.request.LoginRequests;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.services.Userdetailsimpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AuthController {


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtils jwtUtils;



    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequests loginRequests){

        Authentication authentication;
        try{
            authentication=authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequests.getUsername(),
                            loginRequests.getPassword())
            );

        }catch (AuthenticationException e){
            Map<String,Object> map=new HashMap<>();
            map.put("message","Bad Credentials");
            map.put("status",false);
            return new ResponseEntity<Object>(map, HttpStatus.UNAUTHORIZED);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Userdetailsimpl userDetails= (Userdetailsimpl) authentication.getPrincipal();
        String jwtToken=jwtUtils.getTokenFromUserName(userDetails);
        List<String> roles=userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority).toList();

        UserInfoResponse response=new UserInfoResponse(userDetails.getId(),userDetails.getUsername(),jwtToken,roles);

        return ResponseEntity.ok(response);
    }
}
