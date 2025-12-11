package com.ecommerce.project.controller;


import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repository.RoleRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.security.jwt.JWTUtils;
import com.ecommerce.project.security.request.LoginRequests;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.services.Userdetailsimpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RoleRepository roleRepository;



    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequests loginRequests) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequests.getUsername(),
                            loginRequests.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad Credentials");
            map.put("status", false);
            return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
        }

        // ✅ Set the authenticated user in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // ✅ Get authenticated user details
        Userdetailsimpl userDetails = (Userdetailsimpl) authentication.getPrincipal();

        // ✅ Generate JWT Cookie (for browser usage)
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        // ✅ Generate JWT Token String (for Swagger/Postman)
        String jwtToken = jwtUtils.getTokenFromUserName(userDetails.getUsername());

        // ✅ Extract user roles
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // ✅ Create response that includes the token
        UserInfoResponse response = new UserInfoResponse(
                userDetails.getId(),
                jwtToken, // Added: include token here
                userDetails.getUsername(),
                roles
        );

        // ✅ Return cookie (for browser) + response body (for Swagger/Postman)
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }


    @PostMapping("/signup")
    public ResponseEntity<?>registerUser(@Valid @RequestBody SignupRequest signupRequest ){

        //validations added for existing username and email
          if(userRepository.existsByUserName(signupRequest.getUsername())){
              return  ResponseEntity.badRequest()
                      .body(new MessageResponse("Error: Username already taken!"));
          }

        if(userRepository.existsByEmail(signupRequest.getEmail())){
            return  ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: email already taken!"));
        }

        //Create the user ---->By mapping it to signupRequest--->as in db
        User user=new User(
                signupRequest.getUsername(),
                signupRequest.getEmail(),
                passwordEncoder.encode(signupRequest.getPassword())
        );

        //Assign the role for the user

        Set<String>strRoles=signupRequest.getRole();
        Set<Role>roles=new HashSet<>();


        if(strRoles==null) {
            Role userRole = roleRepository
                    .findByRoleName(AppRole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        }
        else{
            strRoles.forEach(role->{
                switch (role){
                    case "admin":
                        Role adminRole = roleRepository
                                .findByRoleName(AppRole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);
                        break;
                    case "seller":
                        Role sellerRole = roleRepository
                                .findByRoleName(AppRole.ROLE_SELLER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(sellerRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });


        }

        user.setRoles(roles);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User Registered successfully"));
    }


    @GetMapping("/username")
    public String getCurrentUserName(Authentication authentication){
        if(authentication!=null){
            return authentication.getName();
        }else{
            return null;
        }
    }


    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser(Authentication authentication){
        Userdetailsimpl userDetails= (Userdetailsimpl) authentication.getPrincipal();
        List<String> roles=userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority).toList();

        UserInfoResponse response=new UserInfoResponse(userDetails.getId(),userDetails.getUsername(),roles);

        return ResponseEntity.ok().body(response);

    }

    @PostMapping("/signout")
    public ResponseEntity<?>signOutUser(){
        ResponseCookie cookie=jwtUtils.generateCleanJwtCookie();
        return  ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,cookie.toString())
                .body(new MessageResponse("You have been signed out!!"));
    }
}
